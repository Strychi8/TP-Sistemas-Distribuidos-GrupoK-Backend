import { resolveVehicleOperationalStates } from '../utils/vehicleStatus.js';

const timestamp = (value) => ({
  seconds: String(Math.floor(new Date(value).getTime() / 1000)),
  nanos: 0,
});

describe('resolveVehicleOperationalStates', () => {
  const now = new Date('2026-11-24T15:06:00.000Z');
  const vehicles = [
    { idVehiculo: '1', estado: 'RESERVADO' },
    { idVehiculo: '2', estado: 'EN_ALQUILER' },
    { idVehiculo: '3', estado: 'RESERVADO' },
  ];

  it('deriva EN_ALQUILER para una reserva vigente y DISPONIBLE cuando vence', () => {
    const reservations = [
      {
        idVehiculo: '1',
        fechaInicio: timestamp('2026-11-22T17:00:00.000Z'),
        fechaFin: timestamp('2026-11-24T16:00:00.000Z'),
      },
      {
        idVehiculo: '2',
        fechaInicio: timestamp('2026-11-22T17:00:00.000Z'),
        fechaFin: timestamp('2026-11-24T13:00:00.000Z'),
      },
    ];

    expect(resolveVehicleOperationalStates(vehicles, reservations, now).map(({ estado }) => estado))
      .toEqual(['EN_ALQUILER', 'DISPONIBLE', 'DISPONIBLE']);
  });

  it('mantiene el vehículo RESERVADO para una reserva confirmada futura', () => {
    const reservations = [{
      idVehiculo: '3',
      fechaInicio: timestamp('2026-11-25T10:00:00.000Z'),
      fechaFin: timestamp('2026-11-25T15:00:00.000Z'),
    }];

    expect(resolveVehicleOperationalStates(vehicles, reservations, now).map(({ estado }) => estado))
      .toEqual(['DISPONIBLE', 'DISPONIBLE', 'RESERVADO']);
  });

  it('considera finalizado el alquiler al llegar exactamente la fecha y hora de fin', () => {
    const reservations = [{
      idVehiculo: '1',
      fechaInicio: timestamp('2026-11-22T17:00:00.000Z'),
      fechaFin: timestamp('2026-11-24T15:06:00.000Z'),
    }];

    expect(resolveVehicleOperationalStates(vehicles, reservations, now)[0].estado)
      .toBe('DISPONIBLE');
  });
});
