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

  """Representaciónn de un vehículo de la flota"""
  type Vehiculo {
    idVehiculo: ID!
    patente: String!
    marca: String!
    modelo: String!
    anio: Int!
    color: String
    tipoVehiculo: TipoVehiculo!
    precioDiario: Float!
    estado: EstadoVehiculo!
    activo: Boolean!
  }

  """Filtros opcionales para buscar vehículos disponibles."""
  input FiltroDisponibilidad {
    tipoVehiculo: TipoVehiculo
    marca: String
    modelo: String
    precioMin: Float
    precioMax: Float
  }

  """Registro del historial de un alquiler."""
  type HistorialAlquiler {
    vehiculo: String!
    patente: String!
    fechaInicio: String!
    fechaFinalizacion: String!
    cantidadDias: Int!
    importeTotal: Float!
    estado: EstadoReserva!
  }

  type Query {
    vehiculos: [Vehiculo!]!
    vehiculosDisponibles(
      inicio: String!
      fin: String!
      filtro: FiltroDisponibilidad
    ): [Vehiculo!]!
    
    historialAlquileres(
      idCliente: ID!
    ): [HistorialAlquiler!]!
    
    reservas(
      filtro: FiltroReserva
    ): [Reserva!]!
  }

  """Representación de un cliente para uso en las reservas"""
  type ReservaCliente {
    idCliente: ID!
    nombre: String!
    apellido: String!
    dni: String!
    email: String!
  }

  """Representación de una reserva"""
  type Reserva {
    idReserva: ID!
    cliente: ReservaCliente!
    vehiculo: Vehiculo!
    fechaInicio: String!
    fechaFin: String!
    importeTotal: Float!
    precioDiario: Float!
    estado: EstadoReserva!
  }

  """Filtros opcionales para buscar reservas."""
  input FiltroReserva {
    idCliente: ID
    idVehiculo: ID
    tipoVehiculo: TipoVehiculo
    estado: EstadoReserva
    fechaDesde: String
    fechaHasta: String
  }
`);
