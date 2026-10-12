import { jest } from '@jest/globals';
import request from 'supertest';

// 1. Mock de los clientes gRPC ANTES de importar la app
jest.unstable_mockModule('../clients/grpc.js', () => ({
  vehicleClient: {
    GetVehiculos: jest.fn(),
  },
  rentalClient: {
    GetHistorialAlquileres: jest.fn(),
    GetReservas: jest.fn(),
  },
  customerClient: {
    GetClienteById: jest.fn()
  }
}));

// 2. Mock del authMiddleware para poder testear los endpoints protegidos
jest.unstable_mockModule('../middleware/auth.middleware.js', () => ({
  authMiddleware: (req, res, next) => {
    req.user = { sub: 'cliente@ejemplo.com', roles: 'CLIENTE' };
    next();
  }
}));

// Importar los mocks y la app
const { vehicleClient, rentalClient } = await import('../clients/grpc.js');
const { default: app } = await import('../index.js');

describe('GraphQL Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Query: vehiculos', () => {
    test('debe retornar lista de vehículos', async () => {
      vehicleClient.GetVehiculos.mockImplementation((req, meta, cb) => {
        cb(null, {
          vehicles: [
            {
              idVehiculo: '1',
              patente: 'ABC123XXX',
              marca: 'Ford',
              modelo: 'Focus',
              anio: 2020,
              color: 'Azul',
              tipoVehiculo: 'AUTO',
              precioDiario: 15000,
              estado: 'DISPONIBLE',
              activo: true
            }
          ]
        });
      });

      const response = await request(app)
        .post('/graphql')
        .set('Authorization', 'Bearer token_simulado')
        .send({
          query: `
            query {
              vehiculos {
                idVehiculo
                patente
                marca
                tipoVehiculo
                estado
              }
            }
          `
        });

      expect(response.status).toBe(200);
      expect(response.body.data.vehiculos).toHaveLength(1);
      expect(response.body.data.vehiculos[0]).toEqual({
        idVehiculo: '1',
        patente: 'ABC123XXX',
        marca: 'Ford',
        tipoVehiculo: 'SEDAN', // Se aplica mapTipoVehiculoGrpcToUi('AUTO') -> 'SEDAN'
        estado: 'DISPONIBLE'
      });
    });
  });

  describe('Query: historialAlquileres', () => {
    test('debe retornar el historial de alquileres de un cliente', async () => {
      rentalClient.GetHistorialAlquileres.mockImplementation((req, meta, cb) => {
        cb(null, {
          historial: [
            {
              vehiculo: 'Ford Focus',
              patente: 'ABC123XXX',
              fechaInicio: { seconds: 1704067200, nanos: 0 }, // 1/1/2024
              fechaFinalizacion: { seconds: 1704326400, nanos: 0 }, // 4/1/2024
              cantidadDias: 3,
              importeTotal: 45000,
              estado: 'FINALIZADA'
            }
          ]
        });
      });

      const response = await request(app)
        .post('/graphql')
        .set('Authorization', 'Bearer token_simulado')
        .send({
          query: `
            query {
              historialAlquileres(idCliente: "10") {
                vehiculo
                patente
                estado
                cantidadDias
                importeTotal
              }
            }
          `
        });

      expect(response.status).toBe(200);
      expect(response.body.data.historialAlquileres).toHaveLength(1);
      expect(response.body.data.historialAlquileres[0]).toEqual({
        vehiculo: 'Ford Focus',
        patente: 'ABC123XXX',
        estado: 'FINALIZADA',
        cantidadDias: 3,
        importeTotal: 45000
      });
      expect(rentalClient.GetHistorialAlquileres).toHaveBeenCalledWith(
        expect.objectContaining({ idCliente: '10' }),
        expect.anything(),
        expect.any(Function)
      );
    });
  });

  describe('Query: reservas', () => {
    test('debe retornar lista de reservas', async () => {
      rentalClient.GetReservas.mockImplementation((req, meta, cb) => {
        cb(null, {
          reservas: [
            {
              idReserva: '100',
              cliente: {
                idCliente: '10',
                nombre: 'Juan',
                apellido: 'Perez',
                dni: '12345678',
                email: 'juan@test.com'
              },
              vehiculo: {
                idVehiculo: '1',
                patente: 'ABC123XXX',
                tipoVehiculo: 'AUTO'
              },
              fechaInicio: { seconds: 1704067200, nanos: 0 },
              fechaFin: { seconds: 1704326400, nanos: 0 },
              importeTotal: 45000,
              precioDiario: 15000,
              estado: 'CONFIRMADA'
            }
          ]
        });
      });

      const response = await request(app)
        .post('/graphql')
        .set('Authorization', 'Bearer token_simulado')
        .send({
          query: `
            query {
              reservas {
                idReserva
                estado
                importeTotal
                cliente {
                  nombre
                }
                vehiculo {
                  patente
                  tipoVehiculo
                }
              }
            }
          `
        });

      expect(response.status).toBe(200);
      expect(response.body.errors).toBeUndefined();
      expect(response.body.data.reservas).toHaveLength(1);
      expect(response.body.data.reservas[0]).toEqual({
        idReserva: '100',
        estado: 'CONFIRMADA',
        importeTotal: 45000,
        cliente: {
          nombre: 'Juan'
        },
        vehiculo: {
          patente: 'ABC123XXX',
          tipoVehiculo: 'SEDAN'
        }
      });
    });
  });
});
