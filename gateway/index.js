import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import swaggerUi from 'swagger-ui-express';
import { swaggerDocument } from './swagger.js';
import { errorMiddleware } from './middleware/error.middleware.js';
import graphqlRouter from './graphql/index.js';
import authRouter from './routes/auth.routes.js';
import clienteRouter from './routes/cliente.routes.js';
import vehiculoRouter from './routes/vehiculo.routes.js';
import reservaRouter from './routes/reserva.routes.js';

import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Configurar variables de entorno (busca en gateway/.env y en ../.env)
dotenv.config();
dotenv.config({ path: path.resolve(__dirname, '../.env') });

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

// Documentación Swagger / OpenAPI
app.use('/docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));
app.get('/api-docs', (req, res) => res.json(swaggerDocument));

// Rutas de GraphQL
app.use('/graphql', graphqlRouter);
app.get('/graphiql', (req, res) => res.redirect('/graphql'));

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
