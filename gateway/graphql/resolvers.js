import { vehicleClient, rentalClient } from '../clients/grpc.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';
import { formatGrpcTimestamp, parseDateToGrpcTimestamp } from '../utils/mappers.js';

// Mapeos
const mapTipoVehiculoGrpcToUi = (tipoGrpc) => {
  const map = {
    'AUTO': 'SEDAN',
    'CAMIONETA': 'PICKUP',
    'MOTO': 'SEDAN'
  };
  return map[tipoGrpc] || 'SEDAN';
};

const mapTipoVehiculoUiToGrpc = (tipoUi) => {
  const map = {
    'SEDAN': 'AUTO',
    'SUV': 'CAMIONETA',
    'PICKUP': 'CAMIONETA',
    'COUPE': 'AUTO',
    'HATCHBACK': 'AUTO'
  };
  return map[tipoUi] || 'AUTO';
};

const mapVehiculo = (v) => ({
  idVehiculo: v.idVehiculo,
  patente: v.patente,
  marca: v.marca,
  modelo: v.modelo,
  anio: v.anio,
  color: v.color,
  tipoVehiculo: mapTipoVehiculoGrpcToUi(v.tipoVehiculo),
  precioDiario: v.precioDiario,
  estado: v.estado,
  activo: v.activo
});

export const resolvers = {
  vehiculos: (_, context) => {
    return new Promise((resolve, reject) => {
      const meta = createGrpcMetadata(context.req);
      vehicleClient.GetVehiculos({ page: 0, size: 100 }, meta, (err, response) => {
        if (err) return reject(err);
        resolve((response.vehicles || []).map(mapVehiculo));
      });
    });
  },

  vehiculosDisponibles: async (args, context) => {
    try {
      const meta = createGrpcMetadata(context.req);
      
      const req = {
        page: 0,
        size: 100
      };
      
      if (args.filtro) {
        if (args.filtro.tipoVehiculo) req.tipoVehiculo = mapTipoVehiculoUiToGrpc(args.filtro.tipoVehiculo);
        if (args.filtro.marca) req.marca = args.filtro.marca;
        if (args.filtro.modelo) req.modelo = args.filtro.modelo;
        if (args.filtro.precioMin) req.precioMin = args.filtro.precioMin;
        if (args.filtro.precioMax) req.precioMax = args.filtro.precioMax;
      }
      
      // 1. Consultar vehicle-service
      const candidatos = await new Promise((resolve, reject) => {
        vehicleClient.GetVehiculos(req, meta, (err, response) => {
          if (err) return reject(err);
          resolve(response.vehicles || []);
        });
      });

      // 2. Consultar rental-service por cada candidato
      const disponibles = [];
      const fechaInicio = parseDateToGrpcTimestamp(args.inicio);
      const fechaFin = parseDateToGrpcTimestamp(args.fin);

      for (const vehiculo of candidatos) {
        const solapado = await new Promise((resolve, reject) => {
          rentalClient.VerificarSolapamientoVehiculo({
            idVehiculo: vehiculo.idVehiculo,
            fechaInicio,
            fechaFin
          }, meta, (err, res) => {
            if (err) return reject(err);
            resolve(res.solapado);
          });
        });
        
        // 3. Conservar solo los vehículos sin solapamiento
        if (!solapado) {
          disponibles.push(vehiculo);
        }
      }

      return disponibles.map(mapVehiculo);
    } catch (err) {
      throw err;
    }
  },

  historialAlquileres: (args, context) => {
    return new Promise((resolve, reject) => {
      const meta = createGrpcMetadata(context.req);
      rentalClient.GetHistorialAlquileres({ idCliente: args.idCliente }, meta, (err, response) => {
        if (err) return reject(err);
        resolve((response.historial || []).map(h => ({
          vehiculo: h.vehiculo,
          patente: h.patente,
          fechaInicio: formatGrpcTimestamp(h.fechaInicio),
          fechaFinalizacion: formatGrpcTimestamp(h.fechaFinalizacion),
          cantidadDias: h.cantidadDias,
          importeTotal: h.importeTotal,
          estado: h.estado
        })));
      });
    });
  },

  reservas: (args, context) => {
    return new Promise((resolve, reject) => {
      const meta = createGrpcMetadata(context.req);
      
      const req = {};
      if (args.filtro) {
        if (args.filtro.idCliente) req.idCliente = args.filtro.idCliente;
        if (args.filtro.idVehiculo) req.idVehiculo = args.filtro.idVehiculo;
        if (args.filtro.estado) req.estado = args.filtro.estado;
        if (args.filtro.fechaDesde) req.fechaDesde = parseDateToGrpcTimestamp(args.filtro.fechaDesde);
        if (args.filtro.fechaHasta) req.fechaHasta = parseDateToGrpcTimestamp(args.filtro.fechaHasta);
      }
      
      rentalClient.GetReservas(req, meta, (err, response) => {
        if (err) return reject(err);
        resolve((response.reservas || []).map(r => ({
          idReserva: r.idReserva,
          cliente: {
            idCliente: r.cliente.idCliente,
            nombre: r.cliente.nombre,
            apellido: r.cliente.apellido,
            dni: r.cliente.dni,
            email: r.cliente.email
          },
          vehiculo: mapVehiculo(r.vehiculo),
          fechaInicio: formatGrpcTimestamp(r.fechaInicio),
          fechaFin: formatGrpcTimestamp(r.fechaFin),
          importeTotal: r.importeTotal,
          precioDiario: r.precioDiario,
          estado: r.estado
        })));
      });
    });
  }
};
