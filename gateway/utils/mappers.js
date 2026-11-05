/**
 * Transforma un objeto google.protobuf.Timestamp que viene desde gRPC
 * al formato String (ISO 8601) que espera consumir el frontend.
 * 
 * @param {Object} timestamp - Objeto { seconds, nanos } de gRPC.
 * @returns {String|null} Fecha en formato ISO 8601 (ej. '2023-10-10T15:30:00.000Z')
 */
export const formatGrpcTimestamp = (timestamp) => {
  if (!timestamp || !timestamp.seconds) return null;
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
 * Normaliza los nombres de enums con prefijos de Buf Lint a los valores esperados por el frontend.
 */
export const mapEstadoReserva = (estadoGrpc) => {
  const map = {
    'ESTADO_RESERVA_UNSPECIFIED': null,
    'ESTADO_RESERVA_CONFIRMADA': 'CONFIRMADA',
    'ESTADO_RESERVA_CANCELADA': 'CANCELADA',
    'ESTADO_RESERVA_FINALIZADA': 'FINALIZADA',
    'CONFIRMADA': 'CONFIRMADA',
    'CANCELADA': 'CANCELADA',
    'FINALIZADA': 'FINALIZADA',
    0: null,
    1: 'CONFIRMADA',
    2: 'CANCELADA',
    3: 'FINALIZADA',
    '0': null,
    '1': 'CONFIRMADA',
    '2': 'CANCELADA',
    '3': 'FINALIZADA'
  };
  return map[estadoGrpc] !== undefined ? map[estadoGrpc] : estadoGrpc;
};

/**
 * Mapa de traducción para EstadoVehiculo.
 */
export const mapEstadoVehiculo = (estadoGrpc) => {
  const map = {
    'ESTADO_VEHICULO_UNSPECIFIED': null,
    'ESTADO_VEHICULO_DISPONIBLE': 'DISPONIBLE',
    'ESTADO_VEHICULO_RESERVADO': 'RESERVADO',
    'ESTADO_VEHICULO_EN_ALQUILER': 'EN_ALQUILER',
    'DISPONIBLE': 'DISPONIBLE',
    'RESERVADO': 'RESERVADO',
    'EN_ALQUILER': 'EN_ALQUILER',
    0: null,
    1: 'DISPONIBLE',
    2: 'RESERVADO',
    3: 'EN_ALQUILER',
    '0': null,
    '1': 'DISPONIBLE',
    '2': 'RESERVADO',
    '3': 'EN_ALQUILER'
  };
  return map[estadoGrpc] !== undefined ? map[estadoGrpc] : estadoGrpc;
};

/**
 * Mapa de traducción para TipoVehiculo.
 */
export const mapTipoVehiculo = (tipoGrpc) => {
  const map = {
    'TIPO_VEHICULO_UNSPECIFIED': null,
    'TIPO_VEHICULO_SEDAN': 'SEDAN',
    'TIPO_VEHICULO_SUV': 'SUV',
    'TIPO_VEHICULO_PICKUP': 'PICKUP',
    'TIPO_VEHICULO_COUPE': 'COUPE',
    'TIPO_VEHICULO_HATCHBACK': 'HATCHBACK',
    'AUTO': 'SEDAN',
    'SEDAN': 'SEDAN',
    'SUV': 'SUV',
    'PICKUP': 'PICKUP',
    'COUPE': 'COUPE',
    'HATCHBACK': 'HATCHBACK',
    0: null,
    1: 'SEDAN',
    2: 'SUV',
    3: 'PICKUP',
    4: 'COUPE',
    5: 'HATCHBACK'
  };
  return map[tipoGrpc] || tipoGrpc;
};

/**
 * Traduce un estado de vehículo a la convención con prefijo del contrato gRPC.
 */
export const toProtoEstadoVehiculo = (estado) => {
  if (!estado) return 'ESTADO_VEHICULO_UNSPECIFIED';
  if (String(estado).startsWith('ESTADO_VEHICULO_')) return estado;
  return `ESTADO_VEHICULO_${estado}`;
};

/**
 * Traduce un tipo de vehículo a la convención con prefijo del contrato gRPC.
 */
export const toProtoTipoVehiculo = (tipo) => {
  if (!tipo) return 'TIPO_VEHICULO_UNSPECIFIED';
  if (String(tipo).startsWith('TIPO_VEHICULO_')) return tipo;
  if (tipo === 'AUTO') return 'TIPO_VEHICULO_SEDAN';
  return `TIPO_VEHICULO_${tipo}`;
};
