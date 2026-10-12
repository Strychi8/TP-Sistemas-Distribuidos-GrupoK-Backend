import jwt from 'jsonwebtoken';
import express from 'express';
import { customerClient } from '../clients/grpc.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';

const router = express.Router();

router.post('/login', (req, res, next) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ message: 'Email y password son requeridos' });
  }

  customerClient.Login({ email, password }, (err, response) => {
    if (err) return next(err);
    try {
      const decoded = jwt.decode(response.token);
      const roles = decoded && decoded.roles ? (Array.isArray(decoded.roles) ? decoded.roles : [decoded.roles]) : [];
      return res.json({
        token: response.token,
        email: decoded?.sub || email,
        roles: roles
      });
    } catch {
      return res.json(response);
    }
  });
});

router.post('/logout', authMiddleware, (req, res, next) => {
  const authHeader = req.headers.authorization;
  const token = authHeader.split(" ")[1];

  customerClient.Logout({ token }, createGrpcMetadata(req), (err, response) => {
    if (err) return next(err);
    res.json({ message: 'Sesión cerrada correctamente' });
  });
});

export default router;
