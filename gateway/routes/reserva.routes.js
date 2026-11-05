import express from 'express';
import { rentalClient, customerClient, vehicleClient } from '../clients/grpc.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';
import { parseDateToGrpcTimestamp, mapEstadoVehiculo, formatGrpcTimestamp, mapEstadoReserva, toProtoEstadoVehiculo } from '../utils/mappers.js';

const router = express.Router();

router.use(authMiddleware);

// Alta de reserva (con orquestación gRPC)
router.post('/', async (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const { idVehiculo, fechaInicio, fechaFin } = req.body;

  // 1. Validar fechas: fail-fast local
  const inicioDate = new Date(fechaInicio);
  const finDate = new Date(fechaFin);
  const now = new Date();

  if (isNaN(inicioDate.getTime()) || isNaN(finDate.getTime())) {
    return res.status(400).json({ error: 'Fechas inválidas', message: 'Fechas inválidas' });
  }
  if (inicioDate < now) {
    return res.status(400).json({ error: 'La fecha de inicio de la reserva debe ser futura', message: 'La fecha de inicio de la reserva debe ser futura' });
  }
  if (finDate <= inicioDate) {
    return res.status(400).json({ error: 'La fecha de fin de la reserva debe ser posterior a la fecha de inicio', message: 'La fecha de fin de la reserva debe ser posterior a la fecha de inicio' });
  }

  try {
    // 2. CustomerService.GetMiPerfil: resolvemos el cliente desde el TOKEN (sub),
    //    NO desde el body. Evita que un cliente opere a nombre de otro.
    const cliente = await new Promise((resolve, reject) => {
      customerClient.GetMiPerfil({ email: req.user.sub }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response?.customer || response);
      });
    });

    if (!cliente.activo) {
      return res.status(400).json({ error: 'El cliente se encuentra inactivo', message: 'El cliente se encuentra inactivo' });
    }

    // idCliente derivado de la identidad autenticada (no del payload del cliente).
    const idCliente = cliente.idCliente ?? cliente.id_cliente;

    // 3. VehicleService.GetVehiculoById
    const vehiculo = await new Promise((resolve, reject) => {
      vehicleClient.GetVehiculoById({ id_vehiculo: idVehiculo, idVehiculo }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response?.vehicle || response);
      });
    });

    if (!vehiculo.activo) {
      return res.status(400).json({ error: 'El vehículo se encuentra inactivo', message: 'El vehículo se encuentra inactivo' });
    }

    if (vehiculo.inhabilitado) {
      return res.status(400).json({ error: 'El vehículo se encuentra inhabilitado', message: 'El vehículo se encuentra inhabilitado' });
    }

    const estadoVehiculo = vehiculo.estado !== undefined && vehiculo.estado !== null
      ? mapEstadoVehiculo(vehiculo.estado)
      : 'DISPONIBLE';

    if (estadoVehiculo !== 'DISPONIBLE') {
      return res.status(400).json({ 
        error: 'El vehículo no se encuentra disponible', 
        message: 'El vehículo no se encuentra disponible' 
      });
    }

    const precioDiario = Number(vehiculo.precioDiario ?? vehiculo.precio_diario);

    // 4. RentalService.VerificarSolapamientoVehiculo
    const solapamiento = await new Promise((resolve, reject) => {
      rentalClient.VerificarSolapamientoVehiculo({
        id_vehiculo: idVehiculo,
        idVehiculo: idVehiculo,
        fecha_inicio: parseDateToGrpcTimestamp(inicioDate),
        fechaInicio: parseDateToGrpcTimestamp(inicioDate),
        fecha_fin: parseDateToGrpcTimestamp(finDate),
        fechaFin: parseDateToGrpcTimestamp(finDate)
      }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response); // asume { solapado: boolean }
      });
    });

    if (solapamiento.solapado) {
      return res.status(409).json({ 
        error: 'El vehículo no está disponible en las fechas seleccionadas (solapamiento de reserva)',
        message: 'El vehículo no está disponible en las fechas seleccionadas (solapamiento de reserva)'
      });
    }

    // Calcular importe total
    const diffTime = Math.abs(finDate - inicioDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    const importeTotal = diffDays * precioDiario;

    // 5. RentalService.CrearReserva
    const rawReserva = await new Promise((resolve, reject) => {
      rentalClient.CrearReserva({
        id_cliente: idCliente,
        idCliente: idCliente,
        id_vehiculo: idVehiculo,
        idVehiculo: idVehiculo,
        fecha_inicio: parseDateToGrpcTimestamp(inicioDate),
        fechaInicio: parseDateToGrpcTimestamp(inicioDate),
        fecha_fin: parseDateToGrpcTimestamp(finDate),
        fechaFin: parseDateToGrpcTimestamp(finDate),
        precio_diario: precioDiario,
        precioDiario: precioDiario,
        importe_total: importeTotal,
        importeTotal: importeTotal
      }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response?.reservation || response);
      });
    });

    const reservaId = rawReserva.idReserva ?? rawReserva.id_reserva;

    // 6. Gateway -> VehicleService.ActualizarEstadoVehiculo a RESERVADO
    try {
      await new Promise((resolve, reject) => {
        vehicleClient.ActualizarEstadoVehiculo({
          id_vehiculo: idVehiculo,
          idVehiculo: idVehiculo,
          estado: toProtoEstadoVehiculo('RESERVADO')
        }, meta, (err, response) => {
          if (err) return reject(err);
          resolve(response?.vehicle || response);
        });
      });
    } catch (updateError) {
      // Rollback: Cancelar la reserva si falló la actualización del vehículo
      await new Promise((resolve) => {
        rentalClient.CancelarReserva({ id_reserva: reservaId, idReserva: reservaId }, meta, (err) => {
          if (err) console.error(`Error crítico en rollback de reserva ${reservaId}:`, err);
          resolve();
        });
      });
      
      const error = new Error('Error de consistencia: Falló la actualización del vehículo. Se canceló la reserva (Rollback).');
      error.details = updateError.details;
      throw error;
    }

    // Mapeo explícito gRPC -> ReservaResponseDTO (Contrato Hito 1 UI)
    const fechaIni = rawReserva.fechaInicio ?? rawReserva.fecha_inicio;
    const fechaF = rawReserva.fechaFin ?? rawReserva.fecha_fin;
    const precD = rawReserva.precioDiario ?? rawReserva.precio_diario;
    const impTot = rawReserva.importeTotal ?? rawReserva.importe_total;
    const est = rawReserva.estado;

    const responseDTO = {
      idReserva: Number(reservaId),
      idCliente: Number(rawReserva.idCliente ?? rawReserva.id_cliente),
      idVehiculo: Number(rawReserva.idVehiculo ?? rawReserva.id_vehiculo),
      fechaInicio: formatGrpcTimestamp(fechaIni),
      fechaFin: formatGrpcTimestamp(fechaF),
      precioDiario: Number(precD),
      importeTotal: Number(impTot),
      estadoReserva: mapEstadoReserva(est) || 'CONFIRMADA'
    };

    return res.status(201).json(responseDTO);

  } catch (error) {
    // El manejador global de errores capturará los fallos gRPC (como el 404 de cliente no existe)
    next(error);
  }
});

// Cancelación de reserva (soporta tanto PUT /:id/cancelar como DELETE /:id)
const cancelarHandler = (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const idReserva = req.params.id;
  rentalClient.CancelarReserva({ id_reserva: idReserva, idReserva }, meta, (err, response) => {
    if (err) return next(err);
    const reserva = response?.reservation || response;
    res.json({ message: 'Reserva cancelada correctamente', ...(reserva || {}) });
  });
};

router.put('/:id/cancelar', cancelarHandler);
router.delete('/:id', cancelarHandler);

export default router;
