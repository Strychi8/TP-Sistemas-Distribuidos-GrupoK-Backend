import express from 'express';
import { vehicleClient } from '../clients/grpc.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';

const router = express.Router();

router.use(authMiddleware);

const mapTipoVehiculoUiToGrpc = (tipoUi) => {
  const map = {
    'SEDAN': 'AUTO',
    'SUV': 'CAMIONETA',
    'PICKUP': 'CAMIONETA',
    'COUPE': 'AUTO',
    'HATCHBACK': 'AUTO'
  };
  return map[tipoUi] || 'AUTO';
};

const mapEstadoVehiculoUiToGrpc = (estadoUi) => {
  const map = {
    'DISPONIBLE': 'DISPONIBLE',
    'RESERVADO': 'ALQUILADO',
    'EN_ALQUILER': 'ALQUILADO'
  };
  return map[estadoUi] || 'DISPONIBLE';
};

// Alta
router.post('/', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const data = { ...req.body };
  if (data.tipoVehiculo) data.tipoVehiculo = mapTipoVehiculoUiToGrpc(data.tipoVehiculo);

  vehicleClient.CrearVehiculo(data, meta, (err, response) => {
    if (err) return next(err);
    res.status(201).json(response);
  });
});

// Modificación
router.put('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const data = { ...req.body, idVehiculo: req.params.id };
  if (data.tipoVehiculo) data.tipoVehiculo = mapTipoVehiculoUiToGrpc(data.tipoVehiculo);
  if (data.estado) data.estado = mapEstadoVehiculoUiToGrpc(data.estado);

  vehicleClient.ActualizarVehiculo(data, meta, (err, response) => {
    if (err) return next(err);
    res.json(response);
  });
});

// Baja lógica
router.delete('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  vehicleClient.EliminarVehiculo({ idVehiculo: req.params.id }, meta, (err, response) => {
    if (err) return next(err);
    res.json({ message: 'Vehículo inhabilitado correctamente', ...response });
  });
});

// Consulta de vehículos (ABM admin)
router.get('/', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const reqGrpc = {
    page: req.query.page ? parseInt(req.query.page) : 0,
    size: req.query.size ? parseInt(req.query.size) : 100
  };
  // Podemos pasar filtros si existen en query string
  if (req.query.tipoVehiculo) reqGrpc.tipoVehiculo = mapTipoVehiculoUiToGrpc(req.query.tipoVehiculo);
  if (req.query.marca) reqGrpc.marca = req.query.marca;

  vehicleClient.GetVehiculos(reqGrpc, meta, (err, response) => {
    if (err) return next(err);
    res.json(response);
  });
});

// Consulta vehículo específico
router.get('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  vehicleClient.GetVehiculoById({ idVehiculo: req.params.id }, meta, (err, response) => {
    if (err) return next(err);
    res.json(response);
  });
});

export default router;
