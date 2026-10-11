import express from 'express';
import { vehicleClient } from '../clients/grpc.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';

const router = express.Router();

router.use(authMiddleware);

// Alta
router.post('/', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const data = { ...req.body };

  vehicleClient.CrearVehiculo(data, meta, (err, response) => {
    if (err) return next(err);
    res.status(201).json(response);
  });
});

// Modificación
router.put('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const data = { ...req.body, idVehiculo: req.params.id };

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
  vehicleClient.GetVehiculos({}, meta, (err, response) => {
    if (err) return next(err);
    res.json(response?.vehicles || response || []);
  });
});

// Consulta de vehículos activos (para catálogo y clientes)
router.get('/activos', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  vehicleClient.GetVehiculosActivos({}, meta, (err, response) => {
    if (err) return next(err);
    res.json(response?.vehicles || response || []);
  });
});

// Consulta vehículo por patente
router.get('/patente/:patente', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  vehicleClient.GetVehiculoByPatente({ patente: req.params.patente }, meta, (err, response) => {
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
