import express from 'express';
import { customerClient } from '../clients/grpc.js';
import { authMiddleware } from '../middleware/auth.middleware.js';

const router = express.Router();

router.post('/login', (req, res, next) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ message: 'Email y password son requeridos' });
  }

  customerClient.Login({ email, password }, (err, response) => {
    if (err) return next(err);
    res.json(response); // { token: '...' }
  });
});

router.post('/logout', authMiddleware, (req, res, next) => {
  const authHeader = req.headers.authorization;
  const token = authHeader.split(' ')[1];

  customerClient.Logout({ token }, (err, response) => {
    if (err) return next(err);
    res.json({ message: 'Sesión cerrada correctamente' });
  });
});

export default router;
