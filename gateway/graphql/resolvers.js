import { vehicleClient, rentalClient, customerClient } from '../clients/grpc.js';
import { createGrpcMetadata } from '../utils/grpcMetadata.js';
import { formatGrpcTimestamp, parseDateToGrpcTimestamp, mapTipoVehiculo, mapEstadoVehiculo, mapEstadoReserva, toProtoTipoVehiculo } from '../utils/mappers.js';

const mapVehiculo = (v) => {
  if (!v) return null;
  return {
    idVehiculo: v.idVehiculo ?? v.id_vehiculo,
    patente: v.patente || '',
    marca: v.marca || '',
    modelo: v.modelo || '',
    anio: v.anio || 0,
    color: v.color || '',
    tipoVehiculo: mapTipoVehiculo(v.tipoVehiculo ?? v.tipo_vehiculo),
    precioDiario: v.precioDiario ?? v.precio_diario ?? 0,
    estado: mapEstadoVehiculo(v.estado) || 'DISPONIBLE',
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
        if (args.filtro.tipoVehiculo) {
          const tipo = toProtoTipoVehiculo(args.filtro.tipoVehiculo);
          filtro.tipo_vehiculo = tipo;
          filtro.tipoVehiculo = tipo;
        }
        if (args.filtro.marca) filtro.marca = args.filtro.marca;
        if (args.filtro.modelo) filtro.modelo = args.filtro.modelo;
        if (args.filtro.precioMin != null) {
          filtro.precio_min = args.filtro.precioMin;
          filtro.precioMin = args.filtro.precioMin;
        }
        if (args.filtro.precioMax != null) {
          filtro.precio_max = args.filtro.precioMax;
          filtro.precioMax = args.filtro.precioMax;
        }
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
        const idVeh = vehiculo.idVehiculo ?? vehiculo.id_vehiculo;
        const solapado = await new Promise((resolve, reject) => {
          rentalClient.VerificarSolapamientoVehiculo({
            id_vehiculo: idVeh,
            idVehiculo: idVeh,
            fecha_inicio: fechaInicio,
            fechaInicio,
            fecha_fin: fechaFin,
            fechaFin
          }, meta, (err, res) => {
            if (err) return reject(err);
            resolve(res.solapado);
          });
        });
        
        // 3. Conservar solo los vehículos sin solapamiento, activos, no inhabilitados y disponibles
        const noInhabilitado = !vehiculo.inhabilitado;
        const estadoNorm = mapEstadoVehiculo(vehiculo.estado);
        const disponible = estadoNorm === 'DISPONIBLE' || vehiculo.estado === 1 || vehiculo.estado === undefined;
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
      getHistorialFn({ id_cliente: args.idCliente, idCliente: args.idCliente }, meta, (err, res) => {
        if (err) return reject(err);
        resolve(res);
      });
    });

    const historialList = response.historial || response.history || [];

    // Hidratar vehiculo y patente consultando vehicleClient si vehiculo es ID numérico
    return Promise.all(historialList.map(async (h) => {
      let vehiculoDesc = '';
      let patente = h.patente || '';

      const isNumeric = (val) => val !== null && val !== undefined && !isNaN(Number(val)) && String(val).trim() !== '';

      let idVehiculo = null;
      if ((h.idVehiculo || h.id_vehiculo) && isNumeric(h.idVehiculo || h.id_vehiculo)) {
        idVehiculo = h.idVehiculo || h.id_vehiculo;
      } else if (h.vehiculo && isNumeric(h.vehiculo)) {
        idVehiculo = h.vehiculo;
      } else if (h.vehiculo) {
        vehiculoDesc = String(h.vehiculo);
      }

      if (idVehiculo) {
        try  {
          const v = await new Promise((resolve, reject) => {
            vehicleClient.GetVehiculoById({ id_vehiculo: idVehiculo, idVehiculo }, meta,
              (err, rawRes) => {
                if (err) return reject(err);
                resolve(rawRes?.vehicle || rawRes);
              });
          });
          if (v) {
            vehiculoDesc = `${v.marca} ${v.modelo}`.trim();
            if (!patente) patente = v.patente;
          }
        } catch {
          vehiculoDesc = `Vehículo #${idVehiculo}`;
        }
      }

      const fechaIni = h.fechaInicio ?? h.fecha_inicio;
      const fechaFin = h.fechaFinalizacion ?? h.fecha_finalizacion;
      const cantDias = h.cantidadDias ?? h.cantidad_dias;
      const impTot = h.importeTotal ?? h.importe_total;

      return {
        vehiculo: String(vehiculoDesc || ''),
        patente: String(patente || ''),
        fechaInicio: formatGrpcTimestamp(fechaIni),
        fechaFinalizacion: formatGrpcTimestamp(fechaFin),
        cantidadDias: cantDias,
        importeTotal: impTot,
        estado: mapEstadoReserva(h.estado) || h.estado
      };
    }));
  },

  reservas: async (args, context) => {
    const meta = createGrpcMetadata(context.req);
    
    const req = {};
    if (args.filtro) {
      if (args.filtro.idCliente) {
        req.id_cliente = args.filtro.idCliente;
        req.idCliente = args.filtro.idCliente;
      }
      if (args.filtro.idVehiculo) {
        req.id_vehiculo = args.filtro.idVehiculo;
        req.idVehiculo = args.filtro.idVehiculo;
      }
      if (args.filtro.estado) req.estado = args.filtro.estado;
      if (args.filtro.fechaDesde) {
        const ts = parseDateToGrpcTimestamp(args.filtro.fechaDesde);
        req.fecha_desde = ts;
        req.fechaDesde = ts;
      }
      if (args.filtro.fechaHasta) {
        const ts = parseDateToGrpcTimestamp(args.filtro.fechaHasta);
        req.fecha_hasta = ts;
        req.fechaHasta = ts;
      }
    }
    
    const getReservasFn = (rentalClient.ConsultarReservas || rentalClient.GetReservas).bind(rentalClient);
    const response = await new Promise((resolve, reject) => {
      getReservasFn(req, meta, (err, res) => {
        if (err) return reject(err);
        resolve(res);
      });
    });

    const reservasList = response.reservas || response.reservations || [];

    // Hidratar cliente y vehículo para cada reserva
    return Promise.all(reservasList.map(async (r) => {
      const idCli = r.idCliente ?? r.id_cliente;
      const idVeh = r.idVehiculo ?? r.id_vehiculo;

      let cliente = r.cliente;
      if (!cliente || !cliente.nombre) {
        try {
          cliente = await new Promise((resolve, reject) => {
            customerClient.GetClienteById({ id_cliente: idCli, idCliente: idCli }, meta, (err, rawC) => {
              if (err) return reject(err);
              resolve(rawC?.customer || rawC);
            });
          });
        } catch (err) {
          cliente = { idCliente: idCli, nombre: '', apellido: '', dni: '', email: '' };
        }
      }

      let vehiculo = r.vehiculo;
      if (!vehiculo || !vehiculo.patente) {
        try {
          const v = await new Promise((resolve, reject) => {
            vehicleClient.GetVehiculoById({ id_vehiculo: idVeh, idVehiculo: idVeh }, meta, (err, rawV) => {
              if (err) return reject(err);
              resolve(rawV?.vehicle || rawV);
            });
          });
          vehiculo = mapVehiculo(v);
        } catch (err) {
          vehiculo = {
            idVehiculo: idVeh,
            patente: '',
            marca: '',
            modelo: '',
            anio: 0,
            color: '',
            tipoVehiculo: 'SEDAN',
            precioDiario: r.precioDiario ?? r.precio_diario ?? 0,
            estado: 'DISPONIBLE',
            activo: true,
            inhabilitado: false
          };
        }
      } else {
        vehiculo = mapVehiculo(vehiculo);
      }

      const idRes = r.idReserva ?? r.id_reserva;
      const fechaIni = r.fechaInicio ?? r.fecha_inicio;
      const fechaFin = r.fechaFin ?? r.fecha_fin;
      const impTot = r.importeTotal ?? r.importe_total;
      const precD = r.precioDiario ?? r.precio_diario;
      const resMaint = r.resultadoMantenimiento ?? r.resultado_mantenimiento;

      return {
        idReserva: idRes,
        cliente: {
          idCliente: cliente.idCliente ?? cliente.id_cliente,
          nombre: cliente.nombre,
          apellido: cliente.apellido,
          dni: cliente.dni,
          email: cliente.email
        },
        vehiculo,
        fechaInicio: formatGrpcTimestamp(fechaIni),
        fechaFin: formatGrpcTimestamp(fechaFin),
        importeTotal: impTot,
        precioDiario: precD,
        estado: mapEstadoReserva(r.estado) || r.estado,
        resultadoMantenimiento: resMaint || null
      };
    }));
  }
};
