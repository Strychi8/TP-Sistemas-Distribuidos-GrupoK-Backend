import { jest } from '@jest/globals';

jest.unstable_mockModule('jsonwebtoken', () => ({
  default: {
    verify: jest.fn()
  }
}));

jest.unstable_mockModule('../clients/grpc.js', () => ({
  customerClient: {
    VerificarTokenBlacklist: jest.fn()
  }
}));

const { authMiddleware } = await import('../middleware/auth.middleware.js');
const jwt = (await import('jsonwebtoken')).default;
const { customerClient } = await import('../clients/grpc.js');

describe('Auth Middleware', () => {
  let req;
  let res;
  let next;

  beforeEach(() => {
    req = {
      headers: {}
    };
    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn()
    };
    next = jest.fn();
    jest.clearAllMocks();
  });

  test('debe rechazar si no se proporciona header de autorizacion', () => {
    authMiddleware(req, res, next);
    
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ message: 'No se proporcionó un token de autenticación válido.' });
    expect(next).not.toHaveBeenCalled();
  });

  test('debe rechazar si el header no empieza con Bearer', () => {
    req.headers.authorization = 'InvalidTokenFormat 12345';
    
    authMiddleware(req, res, next);
    
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ message: 'No se proporcionó un token de autenticación válido.' });
    expect(next).not.toHaveBeenCalled();
  });

  test('debe rechazar si el token expiró', () => {
    req.headers.authorization = 'Bearer token_expirado';
    jwt.verify.mockImplementation((token, secret, cb) => {
      cb({ name: 'TokenExpiredError' }, null);
    });

    authMiddleware(req, res, next);

    expect(jwt.verify).toHaveBeenCalled();
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ message: 'El token ha expirado.' });
    expect(next).not.toHaveBeenCalled();
  });

  test('debe rechazar si la firma es inválida', () => {
    req.headers.authorization = 'Bearer token_invalido';
    jwt.verify.mockImplementation((token, secret, cb) => {
      cb({ name: 'JsonWebTokenError' }, null);
    });

    authMiddleware(req, res, next);

    expect(jwt.verify).toHaveBeenCalled();
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ message: 'Firma de token inválida.' });
    expect(next).not.toHaveBeenCalled();
  });

  test('debe rechazar si el token está en la blacklist (revocado)', () => {
    req.headers.authorization = 'Bearer token_valido_revocado';
    jwt.verify.mockImplementation((token, secret, cb) => {
      cb(null, { sub: 'user1', roles: 'CLIENTE' });
    });

    customerClient.VerificarTokenBlacklist.mockImplementation(({ token }, cb) => {
      cb(null, { revocado: true });
    });

    authMiddleware(req, res, next);

    expect(customerClient.VerificarTokenBlacklist).toHaveBeenCalledWith({ token: 'token_valido_revocado' }, expect.any(Function));
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ message: 'El token ha sido revocado (sesión cerrada).' });
    expect(next).not.toHaveBeenCalled();
  });

  test('debe devolver 503 si falla el servicio gRPC al verificar blacklist', () => {
    req.headers.authorization = 'Bearer token_valido';
    jwt.verify.mockImplementation((token, secret, cb) => {
      cb(null, { sub: 'user1', roles: 'CLIENTE' });
    });

    customerClient.VerificarTokenBlacklist.mockImplementation(({ token }, cb) => {
      cb(new Error('gRPC error'), null);
    });

    authMiddleware(req, res, next);

    expect(res.status).toHaveBeenCalledWith(503);
    expect(res.json).toHaveBeenCalledWith({ message: 'Error interno de autenticación (Customer Service no disponible).' });
    expect(next).not.toHaveBeenCalled();
  });

  test('debe llamar a next y setear req.user si el token es válido y no está revocado', () => {
    req.headers.authorization = 'Bearer token_valido';
    jwt.verify.mockImplementation((token, secret, cb) => {
      cb(null, { sub: 'user1', roles: 'CLIENTE,ADMINISTRADOR' });
    });

    customerClient.VerificarTokenBlacklist.mockImplementation(({ token }, cb) => {
      cb(null, { revocado: false });
    });

    authMiddleware(req, res, next);

    expect(next).toHaveBeenCalled();
    expect(req.user).toEqual({
      sub: 'user1',
      roles: 'CLIENTE,ADMINISTRADOR'
    });
  });
});
