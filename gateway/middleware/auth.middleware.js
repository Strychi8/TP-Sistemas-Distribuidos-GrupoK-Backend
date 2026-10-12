import jwt from 'jsonwebtoken';
import { customerClient } from '../clients/grpc.js';

export const authMiddleware = (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ message: 'No se proporcionó un token de autenticación válido.' });
  }

  const token = authHeader.split(' ')[1];
  const secret = process.env.JWT_SECRET || 'secretKey_para_desarrollo_local_123';

  // 1 y 2. Verificar firma HS256 y expiración
  jwt.verify(token, secret, (err, decoded) => {
    if (err) {
      if (err.name === 'TokenExpiredError') {
        return res.status(401).json({ message: 'El token ha expirado.' });
      }
      return res.status(401).json({ message: 'Firma de token inválida.' });
    }

    // 3. Verificar en la Blacklist vía RPC a customer-service
    customerClient.VerificarTokenBlacklist({ token }, (grpcErr, grpcRes) => {
      if (grpcErr) {
        console.error('Error al verificar blacklist:', grpcErr);
        return res.status(503).json({ message: 'Error interno de autenticación (Customer Service no disponible).' });
      }

      if (grpcRes && grpcRes.revocado) {
        return res.status(401).json({ message: 'El token ha sido revocado (sesión cerrada).' });
      }

      // 4. Extraer sub y roles
      // Según Hito 1, los roles podrían venir como array o string. Lo normalizamos.
      const roles = Array.isArray(decoded.roles) ? decoded.roles.join(',') : (decoded.roles || '');
      
      req.user = {
        sub: decoded.sub,
        roles: roles
      };

      next();
    });
  });
};
