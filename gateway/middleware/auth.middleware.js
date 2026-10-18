import jwt from 'jsonwebtoken';
import { customerClient } from '../clients/grpc.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';

export const authMiddleware = (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ message: 'No se proporcionó un token de autenticación válido.' });
  }

  const token = authHeader.split(' ')[1];

  // customer-service firma con la clave BASE64-decodificada
  // (Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))). jsonwebtoken interpreta
  // un string como UTF-8, así que replicamos la decodificación para que los bytes
  // de la clave HMAC coincidan. Sin esto, jwt.verify SIEMPRE falla con 401.
  const secret = Buffer.from(
    process.env.JWT_SECRET || 'c2VjcmV0LWtleS1kZWZpbmlkYS1wYXJhLWVsLXRyYWJham8tcHJhY3RpY28tZ3J1cG8tay0yMDI2LWFwaS1iYWNrZW5k',
    'base64'
  );

  jwt.verify(token, secret, (err, decoded) => {
    if (err) {
      if (err.name === 'TokenExpiredError') {
        return res.status(401).json({ message: 'El token ha expirado.' });
      }
      return res.status(401).json({ message: 'Firma de token inválida.' });
    }

    const roles = Array.isArray(decoded.roles) ? decoded.roles.join(',') : (decoded.roles || '');
    req.user = { sub: decoded.sub, roles };

    // La verificación de blacklist es un RPC interno que exige identidad (authenticated()).
    // Propagamos sub + roles como el resto de las llamadas del gateway.
    customerClient.VerificarTokenBlacklist({ token }, createGrpcMetadata(req), (grpcErr, grpcRes) => {
      if (grpcErr) {
        console.error('Error al verificar blacklist:', grpcErr);
        return res.status(503).json({ message: 'Error interno de autenticación (Customer Service no disponible).' });
      }

      if (grpcRes && grpcRes.revocado) {
        return res.status(401).json({ message: 'El token ha sido revocado (sesión cerrada).' });
      }

      next();
    });
  });
};
