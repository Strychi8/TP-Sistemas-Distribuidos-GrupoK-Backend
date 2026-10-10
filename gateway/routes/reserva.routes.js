import express from 'express';
import { rentalClient, customerClient, vehicleClient } from '../clients/grpc.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';
import { parseDateToGrpcTimestamp } from '../utils/mappers.js';

const router = express.Router();

router.use(authMiddleware);

// Alta de reserva (con orquestación gRPC)
router.post('/', async (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const { idVehiculo, idCliente, fechaInicio, fechaFin } = req.body;

  // 1. Validar fechas: fail-fast local
  const inicioDate = new Date(fechaInicio);
  const finDate = new Date(fechaFin);
  const now = new Date();

  if (isNaN(inicioDate.getTime()) || isNaN(finDate.getTime())) {
    return res.status(400).json({ message: 'Fechas inválidas' });
  }
  if (inicioDate < now) {
    return res.status(400).json({ message: 'La fecha de inicio de la reserva debe ser futura' });
  }
  if (finDate <= inicioDate) {
    return res.status(400).json({ message: 'La fecha de fin de la reserva debe ser posterior a la fecha de inicio' });
  }

  try {
    // 2. CustomerService.GetClienteById
    const cliente = await new Promise((resolve, reject) => {
      customerClient.GetClienteById({ idCliente }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response);
      });
    });

    if (!cliente.activo) {
      return res.status(400).json({ message: 'El cliente se encuentra inactivo' });
    }

    // 3. VehicleService.GetVehiculoById
    const vehiculo = await new Promise((resolve, reject) => {
      vehicleClient.GetVehiculoById({ idVehiculo }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response);
      });
    });

    if (!vehiculo.activo) {
      return res.status(400).json({ message: 'El vehículo se encuentra inactivo' });
    }

    // 4. RentalService.VerificarSolapamientoVehiculo
    const solapamiento = await new Promise((resolve, reject) => {
      rentalClient.VerificarSolapamientoVehiculo({
        idVehiculo,
        fechaInicio: parseDateToGrpcTimestamp(inicioDate),
        fechaFin: parseDateToGrpcTimestamp(finDate)
      }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response); // asume { solapamiento: boolean }
      });
    });

    if (solapamiento.solapado) {
      return res.status(400).json({ message: 'El vehículo no está disponible en las fechas seleccionadas (solapamiento de reserva)' });
    }

    // Calcular importe total
    const diffTime = Math.abs(finDate - inicioDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    const importeTotal = diffDays * vehiculo.precioDiario;

    // 5. RentalService.CrearReserva
    const reserva = await new Promise((resolve, reject) => {
      rentalClient.CrearReserva({
        idCliente,
        idVehiculo,
        fechaInicio: parseDateToGrpcTimestamp(inicioDate),
        fechaFin: parseDateToGrpcTimestamp(finDate),
        precioDiario: vehiculo.precioDiario,
        importeTotal: importeTotal
      }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response);
      });
    });

    // 6. Gateway -> VehicleService.ActualizarEstadoVehiculo a RESERVADO
    await new Promise((resolve, reject) => {
      vehicleClient.ActualizarEstadoVehiculo({
        idVehiculo,
        estado: 'RESERVADO'
      }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response);
      });
    });

    // Mapeo explicito gRPC -> ReservaResponseDTO (Contrato Hito 1 UI)
    const { formatGrpcTimestamp, mapEstadoReserva } = await import('../utils/mappers.js');
    
    const responseDTO = {
      idReserva: reserva.idReserva,
      idCliente: reserva.idCliente,
      idVehiculo: reserva.idVehiculo,
      fechaInicio: formatGrpcTimestamp(reserva.fechaInicio),
      fechaFin: formatGrpcTimestamp(reserva.fechaFin),
      precioDiario: reserva.precioDiario,
      importeTotal: reserva.importeTotal,
      estadoReserva: mapEstadoReserva(reserva.estado) || 'CONFIRMADA'
    };

    return res.status(201).json(responseDTO);

  } catch (error) {
    // El manejador global de errores capturará los fallos gRPC (como el 404 de cliente no existe)
    next(error);
  }
});

// Cancelación de reserva
router.delete('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  rentalClient.CancelarReserva({ idReserva: req.params.id }, meta, (err, response) => {
    if (err) return next(err);
    res.json({ message: 'Reserva cancelada correctamente', ...response });
  });
});

export default router;
