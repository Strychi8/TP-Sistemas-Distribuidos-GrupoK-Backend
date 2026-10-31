/**
 * Transforma un objeto google.protobuf.Timestamp que viene desde gRPC
 * al formato String (ISO 8601) que espera consumir el frontend (Hito 1).
 * 
 * @param {Object} timestamp - Objeto { seconds, nanos } de gRPC.
 * @returns {String|null} Fecha en formato ISO 8601 (ej. '2023-10-10T15:30:00.000Z')
 */
export const formatGrpcTimestamp = (timestamp) => {
  if (!timestamp || !timestamp.seconds) return null;
  // Convertir los segundos (y nanos si existen) a milisegundos
  const millis = (parseInt(timestamp.seconds, 10) * 1000) + 
                 Math.round((timestamp.nanos || 0) / 1000000);
  return new Date(millis).toISOString();
};

/**
 * Transforma un String (ISO 8601) o Date de Node.js 
 * a un objeto google.protobuf.Timestamp para enviar por gRPC.
 * 
 * @param {String|Date} date - Fecha en String o instanciada.
 * @returns {Object} Objeto con { seconds, nanos }
 */
export const parseDateToGrpcTimestamp = (date) => {
  if (!date) return null;
  const d = new Date(date);
  const millis = d.getTime();
  return {
    seconds: Math.floor(millis / 1000),
    nanos: (millis % 1000) * 1000000
  };
};

/**
 * Mapa de traducción para EstadoReserva.
 * (Ajustar valores de la derecha según la tabla exacta entregada en la consigna)
 */
export const mapEstadoReserva = (estadoGrpc) => {
  const map = {
    'CONFIRMADA': 'CONFIRMADA',
    'CANCELADA': 'CANCELADA',
    'FINALIZADA': 'FINALIZADA',
    'ESTADO_RESERVA_UNSPECIFIED': null
  };
  return map[estadoGrpc] || estadoGrpc;
};

/**
 * Mapa de traducción para EstadoVehiculo.
 */
export const mapEstadoVehiculo = (estadoGrpc) => {
  const map = {
    0: null,
    1: 'DISPONIBLE',
    2: 'RESERVADO',
    3: 'EN_ALQUILER',
    '0': null,
    '1': 'DISPONIBLE',
    '2': 'RESERVADO',
    '3': 'EN_ALQUILER',
    'DISPONIBLE': 'DISPONIBLE',
    'RESERVADO': 'RESERVADO',
    'EN_ALQUILER': 'EN_ALQUILER',
    'ESTADO_VEHICULO_UNSPECIFIED': null
  };
  return map[estadoGrpc] !== undefined ? map[estadoGrpc] : estadoGrpc;
};

/**
 * Mapa de traducción para TipoVehiculo.
 */
export const mapTipoVehiculo = (tipoGrpc) => {
  const map = {
    'SEDAN': 'SEDAN',
    'SUV': 'SUV',
    'PICKUP': 'PICKUP',
    'COUPE': 'COUPE',
    'HATCHBACK': 'HATCHBACK',
    'TIPO_VEHICULO_UNSPECIFIED': null
  };
  return map[tipoGrpc] || tipoGrpc;
};
