import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { errorMiddleware } from './middleware/error.middleware.js';
import graphqlRouter from './graphql/index.js';
import authRouter from './routes/auth.routes.js';
import clienteRouter from './routes/cliente.routes.js';
import vehiculoRouter from './routes/vehiculo.routes.js';
import reservaRouter from './routes/reserva.routes.js';

// Configurar variables de entorno
dotenv.config();

const app = express();
const PORT = process.env.GATEWAY_PORT || 3000;

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Rutas de prueba
app.get('/', (req, res) => {
  res.json({ message: 'El API Gateway esta corriendo' });
});

// Rutas de GraphQL
app.use('/graphql', graphqlRouter);

// Rutas REST
app.use('/api/auth', authRouter);
app.use('/api/clientes', clienteRouter);
app.use('/api/vehiculos', vehiculoRouter);
app.use('/api/reservas', reservaRouter);


// Manejo global de errores
app.use(errorMiddleware);

// Iniciar servidor
if (process.env.NODE_ENV !== 'test') {
  app.listen(PORT, () => {
    console.log(`API Gateway escuchando en el puerto ${PORT}`);
  });
}

export default app; // Exportar para testing
