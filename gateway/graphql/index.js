import { graphqlHTTP } from 'express-graphql';
import { schema } from './schema.js';
import { resolvers } from './resolvers.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import express from 'express';

const router = express.Router();

// Middleware de autenticación para todas las rutas de GraphQL
router.use('/', authMiddleware, graphqlHTTP((req) => ({
  schema: schema,
  rootValue: resolvers,
  graphiql: true, // Permitir probar desde el navegador en modo dev
  context: { req },
  customFormatErrorFn: (err) => {
    // Formatear errores gRPC
    return {
      message: err.message,
      code: err.originalError?.code || 500,
      details: err.originalError?.details || null
    };
  }
})));

export default router;
