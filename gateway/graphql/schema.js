import { buildSchema } from 'graphql';

export const schema = buildSchema(`
  """Tipos de vehículos disponibles"""
  enum TipoVehiculo {
    SEDAN
    SUV
    PICKUP
    COUPE
    HATCHBACK
  }

  """Estados posibles de un vehículo"""
  enum EstadoVehiculo {
    DISPONIBLE
    RESERVADO
    EN_ALQUILER
  }

  """Estados posibles de una reserva"""
  enum EstadoReserva {
    CONFIRMADA
    CANCELADA
    FINALIZADA
  }

  """Representación de un vehículo de la flota"""
  type Vehiculo {
    """Identificador único del vehículo."""
    idVehiculo: ID!
    """Patente o matrícula del vehículo."""
    patente: String!
    """Marca del vehículo."""
    marca: String!
    """Modelo del vehículo."""
    modelo: String!
    """Año de fabricación del vehículo."""
    anio: Int!
    """Color del vehículo."""
    color: String
    """Tipo o categoría del vehículo."""
    tipoVehiculo: TipoVehiculo!
    """Precio diario de alquiler del vehículo."""
    precioDiario: Float!
    """Estado actual del vehículo."""
    estado: EstadoVehiculo!
    """Indica si el vehículo se encuentra activo en el sistema."""
    activo: Boolean!
  }

  """Filtros opcionales para buscar vehículos disponibles."""
  input FiltroDisponibilidad {
    """Filtra por tipo de vehículo."""
    tipoVehiculo: TipoVehiculo
    """Filtra por marca del vehículo."""
    marca: String
    """Filtra por modelo del vehículo."""
    modelo: String
    """Precio diario mínimo."""
    precioMin: Float
    """Precio diario máximo."""
    precioMax: Float
  }

  """
  Registro del historial de un alquiler.
  Incluye alquileres finalizados y reservas canceladas.
  """
  type HistorialAlquiler {
    """Marca y modelo del vehículo."""
    vehiculo: String!
    """Patente del vehículo."""
    patente: String!
    """Fecha y hora de inicio del alquiler."""
    fechaInicio: String!
    """Fecha y hora de finalización del alquiler."""
    fechaFinalizacion: String!
    """Cantidad de días del alquiler."""
    cantidadDias: Int!
    """Importe total del alquiler."""
    importeTotal: Float!
    """Estado de la reserva."""
    estado: EstadoReserva!
  }

  type Query {
    """Obtiene todos los vehículos registrados en el sistema."""
    vehiculos: [Vehiculo!]!

    """Obtiene los vehículos disponibles para alquilar a partir de una fecha determinada.
    Permite aplicar filtros opcionales por tipo, marca, modelo y rango de precio."""
    vehiculosDisponibles(
      """Fecha de inicio de la disponibilidad (YYYY-MM-DD)."""
      inicio: String!
      """Fecha de fin (YYYY-MM-DD)."""
      fin: String!
      """Filtros opcionales para restringir la búsqueda."""
      filtro: FiltroDisponibilidad
    ): [Vehiculo!]!
    
    """Consulta el historial de alquileres de un cliente."""
    historialAlquileres(
      """Identificador del cliente."""
      idCliente: ID!
    ): [HistorialAlquiler!]!
    
    """Obtiene las reservas registradas. Permite filtros opcionales."""
    reservas(
      """Filtros opcionales para buscar reservas."""
      filtro: FiltroReserva
    ): [Reserva!]!
  }

  """Representación de un cliente para uso en las reservas"""
  type ReservaCliente {
    """Identificador del cliente."""
    idCliente: ID!
    """Nombre del cliente."""
    nombre: String!
    """Apellido del cliente."""
    apellido: String!
    """Número de documento del cliente."""
    dni: String!
    """Correo electrónico del cliente."""
    email: String!
  }

  """Representación de una reserva"""
  type Reserva {
    """Identificador único de la reserva."""
    idReserva: ID!
    """Información del cliente que realizó la reserva."""
    cliente: ReservaCliente!
    """Vehículo reservado."""
    vehiculo: Vehiculo!
    """Fecha y hora de inicio de la reserva."""
    fechaInicio: String!
    """Fecha y hora de finalización de la reserva."""
    fechaFin: String!
    """Importe total calculado para la reserva."""
    importeTotal: Float!
    """Precio diario pactado para el vehículo."""
    precioDiario: Float!
    """Estado actual de la reserva."""
    estado: EstadoReserva!
  }

  """Filtros opcionales para buscar reservas."""
  input FiltroReserva {
    """Id del cliente (opcional)."""
    idCliente: ID
    """Id del vehículo (opcional)."""
    idVehiculo: ID
    """Tipo de vehículo (opcional)."""
    tipoVehiculo: TipoVehiculo
    """Estado de la reserva (opcional)."""
    estado: EstadoReserva
    """Fecha desde para el rango (opcional)."""
    fechaDesde: String
    """Fecha hasta para el rango (opcional)."""
    fechaHasta: String
  }
`);
