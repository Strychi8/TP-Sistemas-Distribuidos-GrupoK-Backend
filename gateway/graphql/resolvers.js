import { vehicleClient, rentalClient, customerClient } from '../clients/grpc.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';
import { formatGrpcTimestamp, parseDateToGrpcTimestamp } from '../utils/mappers.js';

// Mapeo seguro de TipoVehiculo (soporta valores del modelo y compatibilidad con mocks de test)
const mapTipoVehiculo = (tipo) => {
  if (tipo === 'AUTO') return 'SEDAN';
  return tipo || 'SEDAN';
};

const mapVehiculo = (v) => {
  if (!v) return null;
  return {
    idVehiculo: v.idVehiculo,
    patente: v.patente || '',
    marca: v.marca || '',
    modelo: v.modelo || '',
    anio: v.anio || 0,
    color: v.color || '',
    tipoVehiculo: mapTipoVehiculo(v.tipoVehiculo),
    precioDiario: v.precioDiario || 0,
    estado: v.estado || 'DISPONIBLE',
    activo: v.activo !== undefined ? v.activo : true,
    inhabilitado: v.inhabilitado !== undefined ? v.inhabilitado : false
  };
};

export const resolvers = {
  vehiculos: (_, context) => {
    return new Promise((resolve, reject) => {
      const meta = createGrpcMetadata(context.req);
      vehicleClient.GetVehiculos({}, meta, (err, response) => {
        if (err) return reject(err);
        resolve((response.vehicles || []).map(mapVehiculo));
      });
    });
  },

  vehiculosDisponibles: async (args, context) => {
    try {
      const meta = createGrpcMetadata(context.req);
      
      const filtro = {};
      if (args.filtro) {
        if (args.filtro.tipoVehiculo) filtro.tipoVehiculo = args.filtro.tipoVehiculo;
        if (args.filtro.marca) filtro.marca = args.filtro.marca;
        if (args.filtro.modelo) filtro.modelo = args.filtro.modelo;
        if (args.filtro.precioMin != null) filtro.precioMin = args.filtro.precioMin;
        if (args.filtro.precioMax != null) filtro.precioMax = args.filtro.precioMax;
      }

      // 1. Consultar vehicle-service enviando el filtro a ConsultarDisponibilidad (o GetVehiculos como fallback)
      const consultFn = (vehicleClient.ConsultarDisponibilidad || vehicleClient.GetVehiculos).bind(vehicleClient);
      const response = await new Promise((resolve, reject) => {
        consultFn(filtro, meta, (err, res) => {
          if (err) return reject(err);
          resolve(res);
        });
      });

      const candidatos = response.vehicles || [];

      // 2. Consultar rental-service por cada candidato para verificar solapamiento
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
        
        // 3. Conservar solo los vehículos sin solapamiento, activos, no inhabilitados y disponibles
        const noInhabilitado = !vehiculo.inhabilitado;
        const disponible = vehiculo.estado === 'DISPONIBLE' || vehiculo.estado === 1 || vehiculo.estado === undefined;
        if (!solapado && noInhabilitado && disponible) {
          disponibles.push(vehiculo);
        }
      }

      return disponibles.map(mapVehiculo);
    } catch (err) {
      throw err;
    }
  },

  historialAlquileres: async (args, context) => {
    const meta = createGrpcMetadata(context.req);
    const getHistorialFn = (rentalClient.ConsultarHistorialAlquileres || rentalClient.GetHistorialAlquileres).bind(rentalClient);

    const response = await new Promise((resolve, reject) => {
      getHistorialFn({ idCliente: args.idCliente }, meta, (err, res) => {
        if (err) return reject(err);
        resolve(res);
      });
    });

    const historialList = response.historial || response.history || [];

    // Hidratar vehiculo y patente consultando vehicleClient si vehiculo es ID numerico
    return Promise.all(historialList.map(async (h) => {
      let vehiculoDesc = '';
      let patente = h.patente || '';

      const isNumeric = (val) => val !== null && val !== undefined && !isNaN(Number(val)) && String(val).trim() !== '';

      let idVehiculo = null;
      if (h.idVehiculo && isNumeric(h.idVehiculo)) {
        idVehiculo = h.idVehiculo;
      } else if (h.vehiculo && isNumeric(h.vehiculo)) {
        idVehiculo = h.vehiculo;
      } else if (h.vehiculo) {
        vehiculoDesc = String(h.vehiculo);
      }

      if (idVehiculo) {
        try  {
          const v = await new Promise((resolve, reject) => {
            vehicleClient.GetVehiculoById({ idVehiculo }, meta,
              (err, veh) => (err ? reject(err) : resolve(veh)));
          });
          if (v) {
            vehiculoDesc = `${v.marca} ${v.modelo}`.trim();
            if (!patente) patente = v.patente;
          }
        } catch {
          vehiculoDesc = `Vehículo #${idVehiculo}`;
        }
      }

      return {
        vehiculo: String(vehiculoDesc || ''),
        patente: String(patente || ''),
        fechaInicio: formatGrpcTimestamp(h.fechaInicio),
        fechaFinalizacion: formatGrpcTimestamp(h.fechaFinalizacion),
        cantidadDias: h.cantidadDias,
        importeTotal: h.importeTotal,
        estado: h.estado
      };
    }));
  },

  reservas: async (args, context) => {
    const meta = createGrpcMetadata(context.req);
    
    const req = {};
    if (args.filtro) {
      if (args.filtro.idCliente) req.idCliente = args.filtro.idCliente;
      if (args.filtro.idVehiculo) req.idVehiculo = args.filtro.idVehiculo;
      if (args.filtro.estado) req.estado = args.filtro.estado;
      if (args.filtro.fechaDesde) req.fechaDesde = parseDateToGrpcTimestamp(args.filtro.fechaDesde);
      if (args.filtro.fechaHasta) req.fechaHasta = parseDateToGrpcTimestamp(args.filtro.fechaHasta);
    }
    
    const getReservasFn = (rentalClient.ConsultarReservas || rentalClient.GetReservas).bind(rentalClient);
    const response = await new Promise((resolve, reject) => {
      getReservasFn(req, meta, (err, res) => {
        if (err) return reject(err);
        resolve(res);
      });
    });

    const reservasList = response.reservas || response.reservations || [];

    // Hidratar cliente y vehiculo para cada reserva
    return Promise.all(reservasList.map(async (r) => {
      let cliente = r.cliente;
      if (!cliente || !cliente.nombre) {
        try {
          cliente = await new Promise((resolve, reject) => {
            customerClient.GetClienteById({ idCliente: r.idCliente }, meta, (err, c) => {
              if (err) return reject(err);
              resolve(c);
            });
          });
        } catch (err) {
          cliente = { idCliente: r.idCliente, nombre: '', apellido: '', dni: '', email: '' };
        }
      }

      let vehiculo = r.vehiculo;
      if (!vehiculo || !vehiculo.patente) {
        try {
          const v = await new Promise((resolve, reject) => {
            vehicleClient.GetVehiculoById({ idVehiculo: r.idVehiculo }, meta, (err, veh) => {
              if (err) return reject(err);
              resolve(veh);
            });
          });
          vehiculo = mapVehiculo(v);
        } catch (err) {
          vehiculo = {
            idVehiculo: r.idVehiculo,
            patente: '',
            marca: '',
            modelo: '',
            anio: 0,
            color: '',
            tipoVehiculo: 'SEDAN',
            precioDiario: r.precioDiario || 0,
            estado: 'DISPONIBLE',
            activo: true,
            inhabilitado: false
          };
        }
      } else {
        vehiculo = mapVehiculo(vehiculo);
      }

      return {
        idReserva: r.idReserva,
        cliente: {
          idCliente: cliente.idCliente,
          nombre: cliente.nombre,
          apellido: cliente.apellido,
          dni: cliente.dni,
          email: cliente.email
        },
        vehiculo,
        fechaInicio: formatGrpcTimestamp(r.fechaInicio),
        fechaFin: formatGrpcTimestamp(r.fechaFin),
        importeTotal: r.importeTotal,
        precioDiario: r.precioDiario,
        estado: r.estado,
        resultadoMantenimiento: r.resultadoMantenimiento || null
      };
    }));
  }
};
