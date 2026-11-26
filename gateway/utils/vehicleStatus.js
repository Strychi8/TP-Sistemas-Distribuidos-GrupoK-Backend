import { rentalClient } from '../clients/grpc.js';
import { toProtoEstadoReserva } from './mappers.js';

const timestampToDate = (timestamp) => {
  const seconds = Number(timestamp?.seconds);
  const nanos = Number(timestamp?.nanos || 0);
  if (!Number.isFinite(seconds) || seconds <= 0 || !Number.isFinite(nanos)) {
    return null;
  }
  return new Date(seconds * 1000 + nanos / 1_000_000);
};

export const resolveVehicleOperationalStates = (vehicles, reservations, now = new Date()) => {
  if (!Array.isArray(vehicles) || !Array.isArray(reservations)) {
    throw new TypeError('Vehicles and confirmed reservations must be arrays');
  }

  const statusByVehicleId = new Map();

  for (const reservation of reservations) {
    const vehicleId = Number(reservation.idVehiculo ?? reservation.id_vehiculo);
    const start = timestampToDate(reservation.fechaInicio ?? reservation.fecha_inicio);
    const end = timestampToDate(reservation.fechaFin ?? reservation.fecha_fin);

    if (!Number.isSafeInteger(vehicleId) || vehicleId <= 0 || !start || !end || end <= start) {
      throw new Error(`Confirmed reservation ${reservation.idReserva ?? reservation.id_reserva ?? 'unknown'} has invalid vehicle or dates`);
    }

    if (end <= now) {
      continue;
    }

    const status = start <= now ? 'EN_ALQUILER' : 'RESERVADO';
    const currentStatus = statusByVehicleId.get(vehicleId);
    if (status === 'EN_ALQUILER' || !currentStatus) {
      statusByVehicleId.set(vehicleId, status);
    }
  }

  return vehicles.map((vehicle) => {
    const vehicleId = Number(vehicle.idVehiculo ?? vehicle.id_vehiculo);
    if (!Number.isSafeInteger(vehicleId) || vehicleId <= 0) {
      throw new Error('Vehicle response has an invalid id');
    }

    return {
      ...vehicle,
      estado: statusByVehicleId.get(vehicleId) || 'DISPONIBLE',
    };
  });
};

export const getVehiclesWithCurrentStatus = async (vehicles, metadata) => {
  if (!rentalClient || typeof rentalClient.ConsultarReservas !== 'function') {
    throw new Error('Rental Service does not expose ConsultarReservas');
  }

  const response = await new Promise((resolve, reject) => {
    rentalClient.ConsultarReservas(
      { estado: toProtoEstadoReserva('CONFIRMADA') },
      metadata,
      (error, result) => {
        if (error) return reject(error);
        resolve(result);
      }
    );
  });

  if (!response || !Array.isArray(response.reservations)) {
    throw new Error('Rental Service returned an invalid reservations response');
  }

  return resolveVehicleOperationalStates(vehicles, response.reservations);
};
