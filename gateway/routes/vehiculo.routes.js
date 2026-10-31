import express from 'express';
import { vehicleClient, customerClient } from '../clients/grpc.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';

const router = express.Router();

router.use(authMiddleware);

// Alta de vehículo externo (incorporación de vehículo propio del cliente)
router.post('/externos', async (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const { patente, marca, modelo, anio, color, tipoVehiculo, precioDiario } = req.body;

  try {
    const cliente = await new Promise((resolve, reject) => {
      customerClient.GetMiPerfil({ email: req.user.sub }, meta, (err, response) => {
        if (err) return reject(err);
        resolve(response);
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
      tipoVehiculo,
      precioDiario: Number(precioDiario),
      documento: cliente.dni,
      clienteId: cliente.idCliente,
    };

    vehicleClient.RegistrarVehiculoExterno(payload, meta, (err, response) => {
      if (err) return next(err);
      res.status(201).json(response);
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
        resolve(response);
      });
    });

    if (!cliente.activo) {
      return res.status(400).json({ error: 'El cliente se encuentra inactivo', message: 'El cliente se encuentra inactivo' });
    }

    vehicleClient.GetVehiculosExternosByCliente({ clienteId: cliente.idCliente }, meta, (err, response) => {
      if (err) return next(err);
      res.json(response?.vehiculosExternos || (Array.isArray(response) ? response : []));
    });
  } catch (error) {
    next(error);
  }
});

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
    res.json({ message: 'Vehículo dado de baja correctamente', ...response });
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
    const list = response?.vehicles || response || [];
    const filtrados = Array.isArray(list)
      ? list.filter(v => !v.inhabilitado && (v.estado === 'DISPONIBLE' || v.estado === 1))
      : list;
    res.json(filtrados);
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
