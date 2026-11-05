import express from 'express';
import { vehicleClient, customerClient } from '../clients/grpc.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';
import { mapEstadoVehiculo, mapTipoVehiculo, toProtoEstadoVehiculo, toProtoTipoVehiculo } from '../utils/mappers.js';

const router = express.Router();

router.use(authMiddleware);

export function normalizeVehicle(v) {
  if (!v) return null;
  const idVehiculo = v.idVehiculo ?? v.id_vehiculo;
  const precioDiario = v.precioDiario ?? v.precio_diario;
  const tipoVehiculo = mapTipoVehiculo(v.tipoVehiculo ?? v.tipo_vehiculo);
  const estado = mapEstadoVehiculo(v.estado);
  return {
    ...v,
    idVehiculo: idVehiculo != null ? Number(idVehiculo) : undefined,
    id_vehiculo: idVehiculo != null ? Number(idVehiculo) : undefined,
    precioDiario: precioDiario != null ? Number(precioDiario) : undefined,
    precio_diario: precioDiario != null ? Number(precioDiario) : undefined,
    tipoVehiculo,
    estado,
  };
}

export function normalizeVehiculoExterno(ve) {
  if (!ve) return null;
  return {
    ...ve,
    idVehiculoExterno: ve.idVehiculoExterno ?? ve.id_vehiculo_externo,
    id_vehiculo_externo: ve.idVehiculoExterno ?? ve.id_vehiculo_externo,
    clienteId: ve.clienteId ?? ve.id_cliente,
    id_cliente: ve.clienteId ?? ve.id_cliente,
    vehiculoId: ve.vehiculoId ?? ve.id_vehiculo,
    id_vehiculo: ve.vehiculoId ?? ve.id_vehiculo,
    precioDiario: ve.precioDiario ?? ve.precio_diario,
    precio_diario: ve.precioDiario ?? ve.precio_diario,
    tipoVehiculo: mapTipoVehiculo(ve.tipoVehiculo ?? ve.tipo_vehiculo),
  };
}

// Alta de vehículo externo (incorporación de vehículo propio del cliente)
router.post('/externos', async (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const { patente, marca, modelo, anio, color, tipoVehiculo, precioDiario } = req.body;

  try {
    const cliente = await new Promise((resolve, reject) => {
      customerClient.GetMiPerfil({ email: req.user.sub }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response?.customer || response);
      });
    });

    if (!cliente.activo) {
      return res.status(400).json({ error: 'El cliente se encuentra inactivo', message: 'El cliente se encuentra inactivo' });
    }

    const payload = {
      patente,
      marca,
      modelo,
      anio: Number(anio),
      color,
      tipo_vehiculo: toProtoTipoVehiculo(tipoVehiculo),
      tipoVehiculo: toProtoTipoVehiculo(tipoVehiculo),
      precio_diario: Number(precioDiario),
      precioDiario: Number(precioDiario),
      documento: cliente.dni,
      id_cliente: cliente.idCliente ?? cliente.id_cliente,
      clienteId: cliente.idCliente ?? cliente.id_cliente,
    };

    vehicleClient.RegistrarVehiculoExterno(payload, meta, (err, response) => {
      if (err) return next(err);
      const creado = response?.vehiculo_externo || response?.vehiculoExterno || response;
      res.status(201).json(normalizeVehiculoExterno(creado));
    });
  } catch (error) {
    next(error);
  }
});

// Listar solicitudes de vehículos externos del cliente autenticado
router.get('/externos', async (req, res, next) => {
  const meta = createGrpcMetadata(req);

  try {
    const cliente = await new Promise((resolve, reject) => {
      customerClient.GetMiPerfil({ email: req.user.sub }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response?.customer || response);
      });
    });

    if (!cliente.activo) {
      return res.status(400).json({ error: 'El cliente se encuentra inactivo', message: 'El cliente se encuentra inactivo' });
    }

    const idCliente = cliente.idCliente ?? cliente.id_cliente;
    vehicleClient.GetVehiculosExternosByCliente({ id_cliente: idCliente, clienteId: idCliente }, meta, (err, response) => {
      if (err) return next(err);
      const list = response?.vehiculos_externos || response?.vehiculosExternos || (Array.isArray(response) ? response : []);
      res.json(list.map(normalizeVehiculoExterno));
    });
  } catch (error) {
    next(error);
  }
});

// Alta
router.post('/', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const data = { ...req.body };
  if (data.tipoVehiculo || data.tipo_vehiculo) {
    const tipo = toProtoTipoVehiculo(data.tipoVehiculo || data.tipo_vehiculo);
    data.tipo_vehiculo = tipo;
    data.tipoVehiculo = tipo;
  }
  if (data.precioDiario || data.precio_diario) {
    const p = Number(data.precioDiario || data.precio_diario);
    data.precio_diario = p;
    data.precioDiario = p;
  }

  vehicleClient.CrearVehiculo(data, meta, (err, response) => {
    if (err) return next(err);
    const vehiculo = response?.vehicle || response;
    res.status(201).json(normalizeVehicle(vehiculo));
  });
});

// Modificación
router.put('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const idVehiculo = req.params.id;
  const data = {
    ...req.body,
    id_vehiculo: idVehiculo,
    idVehiculo: idVehiculo
  };
  if (data.tipoVehiculo || data.tipo_vehiculo) {
    const tipo = toProtoTipoVehiculo(data.tipoVehiculo || data.tipo_vehiculo);
    data.tipo_vehiculo = tipo;
    data.tipoVehiculo = tipo;
  }
  if (data.estado) {
    data.estado = toProtoEstadoVehiculo(data.estado);
  }
  if (data.precioDiario || data.precio_diario) {
    const p = Number(data.precioDiario || data.precio_diario);
    data.precio_diario = p;
    data.precioDiario = p;
  }

  vehicleClient.ActualizarVehiculo(data, meta, (err, response) => {
    if (err) return next(err);
    const vehiculo = response?.vehicle || response;
    res.json(normalizeVehicle(vehiculo));
  });
});

// Baja lógica
router.delete('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const idVehiculo = req.params.id;
  vehicleClient.EliminarVehiculo({ id_vehiculo: idVehiculo, idVehiculo }, meta, (err, response) => {
    if (err) return next(err);
    res.json({ message: 'Vehículo dado de baja correctamente', ...response });
  });
});

// Consulta de vehículos (ABM admin)
router.get('/', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  vehicleClient.GetVehiculos({}, meta, (err, response) => {
    if (err) return next(err);
    const list = response?.vehicles || response || [];
    res.json(Array.isArray(list) ? list.map(normalizeVehicle) : list);
  });
});

// Consulta de vehículos activos (para catálogo y clientes)
router.get('/activos', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  vehicleClient.GetVehiculosActivos({}, meta, (err, response) => {
    if (err) return next(err);
    const list = response?.vehicles || response || [];
    const normalized = Array.isArray(list) ? list.map(normalizeVehicle) : list;
    const filtrados = Array.isArray(normalized)
      ? normalized.filter(v => !v.inhabilitado && (v.estado === 'DISPONIBLE' || v.estado === 1))
      : normalized;
    res.json(filtrados);
  });
});

// Consulta vehículo por patente
router.get('/patente/:patente', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  vehicleClient.GetVehiculoByPatente({ patente: req.params.patente }, meta, (err, response) => {
    if (err) return next(err);
    const vehiculo = response?.vehicle || response;
    res.json(normalizeVehicle(vehiculo));
  });
});

// Consulta vehículo específico
router.get('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const idVehiculo = req.params.id;
  vehicleClient.GetVehiculoById({ id_vehiculo: idVehiculo, idVehiculo }, meta, (err, response) => {
    if (err) return next(err);
    const vehiculo = response?.vehicle || response;
    res.json(normalizeVehicle(vehiculo));
  });
});

export default router;
