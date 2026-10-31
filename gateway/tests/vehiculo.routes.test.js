import { jest } from '@jest/globals';
import request from 'supertest';

// 1. Mock de los clientes gRPC ANTES de importar la app
jest.unstable_mockModule('../clients/grpc.js', () => ({
  rentalClient: {},
  customerClient: {
    GetMiPerfil: jest.fn(),
  },
  vehicleClient: {
    RegistrarVehiculoExterno: jest.fn(),
    GetVehiculosExternosByCliente: jest.fn(),
    GetVehiculoById: jest.fn(),
    GetVehiculos: jest.fn(),
    GetVehiculosActivos: jest.fn(),
  },
}));

// 2. Mock del middleware de auth
jest.unstable_mockModule('../middleware/auth.middleware.js', () => ({
  authMiddleware: (req, res, next) => {
    req.user = { sub: 'cliente@test.com', roles: 'CLIENTE' };
    next();
  }
}));

// Importar los mocks y la app
const { customerClient, vehicleClient } = await import('../clients/grpc.js');
const { default: app } = await import('../index.js');

describe('Rutas de Vehículos Externos (/api/vehiculos/externos)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('POST /api/vehiculos/externos', () => {
    const validBody = {
      patente: 'AA123BB',
      marca: 'Toyota',
      modelo: 'Corolla',
      anio: 2022,
      color: 'Gris',
      tipoVehiculo: 'SEDAN',
      precioDiario: 45000,
      clienteId: 99999, // Intentando suplantar identidad
      documento: '99999999' // Intentando suplantar DNI
    };

    it('Debe ignorar clienteId y documento del body y tomarlos de GetMiPerfil, retornando 201', async () => {
      // Mock perfil del cliente autenticado
      customerClient.GetMiPerfil.mockImplementation((req, meta, cb) => {
        cb(null, {
          idCliente: 15,
          dni: '38123456',
          email: 'cliente@test.com',
          activo: true
        });
      });

      // Mock registro en vehicle-service
      vehicleClient.RegistrarVehiculoExterno.mockImplementation((payload, meta, cb) => {
        cb(null, {
          idVehiculoExterno: 101,
          clienteId: payload.clienteId,
          patente: payload.patente,
          marca: payload.marca,
          modelo: payload.modelo,
          anio: payload.anio,
          color: payload.color,
          tipoVehiculo: payload.tipoVehiculo,
          estado: 'PENDIENTE',
          validacionPatente: 'PENDIENTE',
          validacionTitularidad: 'PENDIENTE',
          vehiculoId: 0,
          precioDiario: payload.precioDiario
        });
      });

      const res = await request(app)
        .post('/api/vehiculos/externos')
        .send(validBody);

      expect(res.status).toBe(201);
      expect(res.body).toHaveProperty('idVehiculoExterno', 101);
      expect(res.body.estado).toBe('PENDIENTE');

      // VERIFICACIÓN CRUCIAL: El payload enviado a gRPC debe tener idCliente=15 y documento=38123456, NO 99999
      expect(vehicleClient.RegistrarVehiculoExterno).toHaveBeenCalledWith(
        expect.objectContaining({
          clienteId: 15,
          documento: '38123456',
          patente: 'AA123BB',
          marca: 'Toyota',
          modelo: 'Corolla',
          anio: 2022,
          color: 'Gris',
          tipoVehiculo: 'SEDAN',
          precioDiario: 45000
        }),
        expect.anything(),
        expect.any(Function)
      );
    });

    it('Debe rechazar con 400 si el cliente se encuentra inactivo', async () => {
      customerClient.GetMiPerfil.mockImplementation((req, meta, cb) => {
        cb(null, {
          idCliente: 15,
          dni: '38123456',
          email: 'cliente@test.com',
          activo: false
        });
      });

      const res = await request(app)
        .post('/api/vehiculos/externos')
        .send(validBody);

      expect(res.status).toBe(400);
      expect(res.body.message).toContain('inactivo');
      expect(vehicleClient.RegistrarVehiculoExterno).not.toHaveBeenCalled();
    });
  });

  describe('GET /api/vehiculos/externos', () => {
    it('Debe obtener las solicitudes del cliente autenticado usando su idCliente resuelto', async () => {
      customerClient.GetMiPerfil.mockImplementation((req, meta, cb) => {
        cb(null, {
          idCliente: 15,
          dni: '38123456',
          email: 'cliente@test.com',
          activo: true
        });
      });

      vehicleClient.GetVehiculosExternosByCliente.mockImplementation((req, meta, cb) => {
        cb(null, {
          vehiculosExternos: [
            {
              idVehiculoExterno: 101,
              clienteId: req.clienteId,
              patente: 'AA123BB',
              marca: 'Toyota',
              modelo: 'Corolla',
              anio: 2022,
              color: 'Gris',
              tipoVehiculo: 'SEDAN',
              estado: 'PENDIENTE',
              validacionPatente: 'PENDIENTE',
              validacionTitularidad: 'PENDIENTE',
              vehiculoId: 0,
              precioDiario: 45000
            }
          ]
        });
      });

      const res = await request(app)
        .get('/api/vehiculos/externos?clienteId=99999'); // Intento de pasar clienteId en query

      expect(res.status).toBe(200);
      expect(Array.isArray(res.body)).toBe(true);
      expect(res.body).toHaveLength(1);
      expect(res.body[0].idVehiculoExterno).toBe(101);

      // Verificamos que la consulta gRPC se hizo con el id 15 del token, no con 99999
      expect(vehicleClient.GetVehiculosExternosByCliente).toHaveBeenCalledWith(
        expect.objectContaining({ clienteId: 15 }),
        expect.anything(),
        expect.any(Function)
      );

      // Verificamos que NO cayó en la ruta GET /:id (orden de rutas correcto)
      expect(vehicleClient.GetVehiculoById).not.toHaveBeenCalled();
    });

    it('Debe rechazar con 400 si el cliente autenticado está inactivo', async () => {
      customerClient.GetMiPerfil.mockImplementation((req, meta, cb) => {
        cb(null, {
          idCliente: 15,
          dni: '38123456',
          email: 'cliente@test.com',
          activo: false
        });
      });

      const res = await request(app)
        .get('/api/vehiculos/externos');

      expect(res.status).toBe(400);
      expect(res.body.message).toContain('inactivo');
      expect(vehicleClient.GetVehiculosExternosByCliente).not.toHaveBeenCalled();
    });
  });
});
