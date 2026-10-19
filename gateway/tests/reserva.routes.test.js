import { jest } from '@jest/globals';
import request from 'supertest';
import jwt from 'jsonwebtoken';

// 1. Mock de los clientes gRPC ANTES de importar la app
jest.unstable_mockModule('../clients/grpc.js', () => ({
  rentalClient: {
    VerificarSolapamientoVehiculo: jest.fn(),
    CrearReserva: jest.fn(),
    CancelarReserva: jest.fn(),
  },
  customerClient: {
    GetClienteById: jest.fn(),
  },
  vehicleClient: {
    GetVehiculoById: jest.fn(),
    ActualizarEstadoVehiculo: jest.fn(),
  },
}));

// 2. Mock del middleware de auth (opcional si queremos bypassearlo o mockear grpcMetadata)
// Para que pase la validación del token de authMiddleware.js:
// - authMiddleware verifica si token no está en blacklist llamando a customerClient.VerificarTokenBlacklist
jest.unstable_mockModule('../middleware/auth.middleware.js', () => ({
  authMiddleware: (req, res, next) => {
    req.user = { sub: '10', roles: 'USER' };
    next();
  }
}));

// Importar los mocks y la app
const { rentalClient, customerClient, vehicleClient } = await import('../clients/grpc.js');
const { default: app } = await import('../index.js');

describe('POST /api/reservas', () => {
  let validPayload;

  beforeEach(() => {
    jest.clearAllMocks();
    
    // Configurar fechas válidas: Inicio = Mañana, Fin = Pasado mañana
    const inicio = new Date();
    inicio.setDate(inicio.getDate() + 1);
    const fin = new Date();
    fin.setDate(fin.getDate() + 2);

    validPayload = {
      idCliente: 10,
      idVehiculo: 5,
      fechaInicio: inicio.toISOString(),
      fechaFin: fin.toISOString()
    };
  });

  it('Debe rechazar si la fecha de inicio no es futura (fail-fast local)', async () => {
    const pastDate = new Date();
    pastDate.setDate(pastDate.getDate() - 1);
    
    validPayload.fechaInicio = pastDate.toISOString();

    const response = await request(app)
      .post('/api/reservas')
      .send(validPayload);

    expect(response.status).toBe(400);
    expect(response.body.message).toBe('La fecha de inicio de la reserva debe ser futura');
    // Verificamos que no se llamó a gRPC
    expect(customerClient.GetClienteById).not.toHaveBeenCalled();
  });

  it('Debe orquestar la reserva exitosamente y retornar HTTP 201 (Verificar orden y formatos)', async () => {
    // Mock 1: Cliente existe y está activo
    customerClient.GetClienteById.mockImplementation((req, meta, cb) => {
      cb(null, { idCliente: 10, activo: true });
    });

    // Mock 2: Vehículo existe, está activo y obtenemos su precio
    vehicleClient.GetVehiculoById.mockImplementation((req, meta, cb) => {
      cb(null, { idVehiculo: 5, activo: true, precioDiario: 5000 });
    });

    // Mock 3: No hay solapamiento
    rentalClient.VerificarSolapamientoVehiculo.mockImplementation((req, meta, cb) => {
      cb(null, { solapado: false });
    });

    // Mock 4: Crear reserva
    rentalClient.CrearReserva.mockImplementation((req, meta, cb) => {
      cb(null, {
        idReserva: 100,
        idCliente: 10,
        idVehiculo: 5,
        fechaInicio: { seconds: Math.floor(new Date(validPayload.fechaInicio).getTime() / 1000), nanos: 0 },
        fechaFin: { seconds: Math.floor(new Date(validPayload.fechaFin).getTime() / 1000), nanos: 0 },
        precioDiario: 5000,
        importeTotal: 5000, // asumiendo 1 día
        estado: 'CONFIRMADA'
      });
    });

    // Mock 5: Actualizar estado vehículo
    vehicleClient.ActualizarEstadoVehiculo.mockImplementation((req, meta, cb) => {
      cb(null, { idVehiculo: 5, estado: 'RESERVADO' });
    });

    const response = await request(app)
      .post('/api/reservas')
      .send(validPayload);

    expect(response.status).toBe(201);
    
    // Validar formato del DTO (Capa de traducción)
    expect(response.body).toHaveProperty('idReserva', 100);
    expect(response.body).toHaveProperty('estadoReserva', 'CONFIRMADA');
    expect(response.body).toHaveProperty('fechaInicio');
    expect(response.body.fechaInicio).toContain('T'); // ISO String format

    // Validar orden de llamadas
    expect(customerClient.GetClienteById).toHaveBeenCalled();
    expect(vehicleClient.GetVehiculoById).toHaveBeenCalled();
    expect(rentalClient.VerificarSolapamientoVehiculo).toHaveBeenCalled();
    expect(rentalClient.CrearReserva).toHaveBeenCalled();
    expect(vehicleClient.ActualizarEstadoVehiculo).toHaveBeenCalledWith(
      expect.objectContaining({ estado: 'RESERVADO' }),
      expect.anything(),
      expect.any(Function)
    );
  });

  it('Debe devolver 400 si hay solapamiento de fechas', async () => {
    customerClient.GetClienteById.mockImplementation((req, meta, cb) => cb(null, { idCliente: 10, activo: true }));
    vehicleClient.GetVehiculoById.mockImplementation((req, meta, cb) => cb(null, { idVehiculo: 5, activo: true, precioDiario: 5000 }));
    
    // Mock: Hay solapamiento
    rentalClient.VerificarSolapamientoVehiculo.mockImplementation((req, meta, cb) => {
      cb(null, { solapado: true });
    });

    const response = await request(app)
      .post('/api/reservas')
      .send(validPayload);

    expect(response.status).toBe(409);
    expect(response.body.message).toContain('solapamiento');
    
    // Verificar que la orquestación se cortó
    expect(rentalClient.CrearReserva).not.toHaveBeenCalled();
    expect(vehicleClient.ActualizarEstadoVehiculo).not.toHaveBeenCalled();
  });
  it('Debe ejecutar rollback (cancelar reserva) si falla la actualización de estado del vehículo', async () => {
    // 1 y 2. Cliente y Vehículo OK
    customerClient.GetClienteById.mockImplementation((req, meta, cb) => cb(null, { idCliente: 10, activo: true }));
    vehicleClient.GetVehiculoById.mockImplementation((req, meta, cb) => cb(null, { idVehiculo: 5, activo: true, precioDiario: 5000 }));
    
    // 3. No hay solapamiento
    rentalClient.VerificarSolapamientoVehiculo.mockImplementation((req, meta, cb) => cb(null, { solapado: false }));

    // 4. Crear reserva OK (Se crea correctamente)
    rentalClient.CrearReserva.mockImplementation((req, meta, cb) => {
      cb(null, { idReserva: 30, idCliente: 10, idVehiculo: 5 });
    });

    // 5. FALLA la actualización del estado del vehículo (Simulamos error gRPC)
    vehicleClient.ActualizarEstadoVehiculo.mockImplementation((req, meta, cb) => {
      cb(new Error('Internal gRPC Error'), null);
    });

    // Mock CancelarReserva
    rentalClient.CancelarReserva.mockImplementation((req, meta, cb) => {
      cb(null, { success: true });
    });

    const response = await request(app)
      .post('/api/reservas')
      .send(validPayload);

    // Debe dar error HTTP 500 por el error gRPC propagado tras el rollback (o lo que configure el error handler)
    expect(response.status).toBe(500);
    expect(response.body.error).toContain('Rollback');

    // VERIFICACIÓN DEL ROLLBACK: Se debe haber llamado a CancelarReserva con el ID 30
    expect(rentalClient.CancelarReserva).toHaveBeenCalledWith(
      expect.objectContaining({ idReserva: 30 }),
      expect.anything(),
      expect.any(Function)
    );
  });
});
