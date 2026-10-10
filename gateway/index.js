import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { errorMiddleware } from './middleware/error.middleware.js';

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

// Manejo global de errores
app.use(errorMiddleware);

// Iniciar servidor
if (process.env.NODE_ENV !== 'test') {
  app.listen(PORT, () => {
    console.log(`API Gateway escuchando en el puerto ${PORT}`);
  });
}

export default app; // Exportar para testing
