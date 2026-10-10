import express from 'express';
import { customerClient } from '../clients/grpc.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';

const router = express.Router();

// Todos los endpoints de clientes requieren autenticación
router.use(authMiddleware);

// Crear cliente (Alta)
router.post('/', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  customerClient.CrearCliente(req.body, meta, (err, response) => {
    if (err) return next(err);
    res.status(201).json(response);
  });
});

// Modificar cliente
router.put('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const data = { ...req.body, idCliente: req.params.id };
  customerClient.ActualizarCliente(data, meta, (err, response) => {
    if (err) return next(err);
    res.json(response);
  });
});

// Baja lógica (Eliminar cliente)
router.delete('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  customerClient.EliminarCliente({ idCliente: req.params.id }, meta, (err, response) => {
    if (err) return next(err);
    res.json({ message: 'Cliente eliminado correctamente', ...response });
  });
});

// Consulta de clientes
router.get('/', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const page = req.query.page ? parseInt(req.query.page) : 0;
  const size = req.query.size ? parseInt(req.query.size) : 100;

  customerClient.GetClientes({ page, size }, meta, (err, response) => {
    if (err) return next(err);
    res.json(response);
  });
});

// Consulta cliente específico
router.get('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  customerClient.GetClienteById({ idCliente: req.params.id }, meta, (err, response) => {
    if (err) return next(err);
    res.json(response);
  });
});

// Mi Perfil
router.get('/me/perfil', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  // req.user.sub suele tener el email en Hito 1 si es Jwt principal
  // pero usaremos el metadata para que customerService lo extraiga
  customerClient.GetMiPerfil({ email: req.user.sub }, meta, (err, response) => {
    if (err) return next(err);
    res.json(response);
  });
});

export default router;
