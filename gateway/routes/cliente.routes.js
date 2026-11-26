import express from 'express';
import { customerClient } from '../clients/grpc.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';
import { toClientListRest, toClientRest, toGoogleDate } from '../utils/clientMapper.js';

const router = express.Router();

// Todos los endpoints de clientes requieren autenticación
router.use(authMiddleware);

// Crear cliente (Alta)
router.post('/', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const rawDate = req.body.fechaNacimiento || req.body.fecha_nacimiento;
  const googleDate = toGoogleDate(rawDate);

  const payload = {
    ...req.body,
    fecha_nacimiento: googleDate,
    fechaNacimiento: googleDate
  };

  customerClient.CrearCliente(payload, meta, (err, response) => {
    if (err) return next(err);
    const cliente = response?.customer || response;
    res.status(201).json(toClientRest(cliente));
  });
});

// Modificar cliente
router.put('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const idCliente = req.params.id;
  const rawDate = req.body.fechaNacimiento || req.body.fecha_nacimiento;
  const googleDate = toGoogleDate(rawDate);

  const payload = {
    ...req.body,
    id_cliente: idCliente,
    idCliente: idCliente,
    fecha_nacimiento: googleDate,
    fechaNacimiento: googleDate
  };

  customerClient.ActualizarCliente(payload, meta, (err, response) => {
    if (err) return next(err);
    const cliente = response?.customer || response;
    res.json(toClientRest(cliente));
  });
});

// Baja lógica (Eliminar cliente)
router.delete('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const idCliente = req.params.id;
  customerClient.EliminarCliente({ id_cliente: idCliente, idCliente }, meta, (err, response) => {
    if (err) return next(err);
    res.json({ message: 'Cliente eliminado correctamente', ...response });
  });
});

// Consulta de clientes
router.get('/', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  customerClient.GetClientes({}, meta, (err, response) => {
    if (err) return next(err);
    res.json(toClientListRest(response?.customers || response || []));
  });
});

// Mi Perfil (soporta /perfil y /me/perfil)
const perfilHandler = (req, res, next) => {
  const meta = createGrpcMetadata(req);
  customerClient.GetMiPerfil({ email: req.user.sub }, meta, (err, response) => {
    if (err) return next(err);
    res.json(toClientRest(response));
  });
};

router.get('/perfil', perfilHandler);
router.get('/me/perfil', perfilHandler);

// Consulta cliente específico
router.get('/:id', (req, res, next) => {
  const meta = createGrpcMetadata(req);
  const idCliente = req.params.id;
  customerClient.GetClienteById({ id_cliente: idCliente, idCliente }, meta, (err, response) => {
    if (err) return next(err);
    const cliente = response?.customer || response;
    res.json(toClientRest(cliente));
  });
});

export default router;
