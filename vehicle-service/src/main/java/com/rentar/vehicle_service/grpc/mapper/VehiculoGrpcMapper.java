package com.rentar.vehicle_service.grpc.mapper;

import com.rentar.vehicle.grpc.*;
import com.rentar.vehicle_service.dto.request.FiltroDisponibilidadDTO;
import com.rentar.vehicle_service.dto.request.VehiculoExternoRequestDTO;
import com.rentar.vehicle_service.dto.request.VehiculoRequestDTO;
import com.rentar.vehicle_service.dto.request.VehiculoUpdateRequestDTO;
import com.rentar.vehicle_service.dto.response.VehiculoExternoResponseDTO;
import com.rentar.vehicle_service.dto.response.VehiculoResponseDTO;
import com.rentar.vehicle_service.enums.EstadoVehiculo;
import com.rentar.vehicle_service.enums.TipoVehiculo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VehiculoGrpcMapper {
    // ---------- proto -> DTO de dominio ----------
    public VehiculoRequestDTO toRequestDTO(CrearVehiculoRequest req) {
        return VehiculoRequestDTO.builder()
                .patente(req.getPatente())
                .marca(req.getMarca())
                .modelo(req.getModelo())
                .anio(req.getAnio())
                .color(req.getColor())
                .tipoVehiculo(mapTipo(req.getTipoVehiculo()))
                .precioDiario(BigDecimal.valueOf(req.getPrecioDiario()))
                .build();
    }

    public VehiculoUpdateRequestDTO toUpdateDTO(ActualizarVehiculoRequest req) {
        return VehiculoUpdateRequestDTO.builder()
                .marca(req.getMarca())
                .modelo(req.getModelo())
                .anio(req.getAnio())
                .color(req.getColor())
                .tipoVehiculo(mapTipo(req.getTipoVehiculo()))
                .precioDiario(BigDecimal.valueOf(req.getPrecioDiario()))
                .estado(toEstado(req.getEstado()))
                .build();
    }

    public FiltroDisponibilidadDTO toFiltroDTO(ConsultarDisponibilidadRequest proto) {
        return FiltroDisponibilidadDTO.builder()
                .tipoVehiculo(proto.hasTipoVehiculo() ? mapTipo(proto.getTipoVehiculo()) : null)
                .marca(proto.hasMarca() ? proto.getMarca() : null)
                .modelo(proto.hasModelo() ? proto.getModelo() : null)
                .precioMin(proto.hasPrecioMin() ? proto.getPrecioMin() : null)
                .precioMax(proto.hasPrecioMax() ? proto.getPrecioMax() : null)
                .build();
    }

    // ---------- DTO -> proto ----------

    public Vehicle toProto(VehiculoResponseDTO dto) {
        return Vehicle.newBuilder()
                .setIdVehiculo(dto.getIdVehiculo())
                .setPatente(dto.getPatente())
                .setMarca(dto.getMarca())
                .setModelo(dto.getModelo())
                .setAnio(dto.getAnio())
                .setColor(dto.getColor() == null ? "" : dto.getColor())
                .setTipoVehiculo(mapTipoToProto(dto.getTipoVehiculo()))
                .setPrecioDiario(dto.getPrecioDiario().doubleValue())
                .setEstado(mapEstadoToProto(dto.getEstado()))
                .setActivo(Boolean.TRUE.equals(dto.getActivo()))
                .setInhabilitado(Boolean.TRUE.equals(dto.getInhabilitado()))
                .build();
    }

    public List<Vehicle> toProtoList(List<VehiculoResponseDTO> dtos) {
        return dtos.stream().map(this::toProto).collect(Collectors.toList());
    }

    // ---------- Enums: Mapeo de prefijos Buf Lint ----------

    public EstadoVehiculo toEstado(com.rentar.vehicle.grpc.EstadoVehiculo proto) {
        if (proto == null || proto == com.rentar.vehicle.grpc.EstadoVehiculo.ESTADO_VEHICULO_UNSPECIFIED) {
            return null;
        }
        String name = proto.name().replace("ESTADO_VEHICULO_", "");
        return EstadoVehiculo.valueOf(name);
    }

    private TipoVehiculo mapTipo(com.rentar.vehicle.grpc.TipoVehiculo proto) {
        if (proto == null || proto == com.rentar.vehicle.grpc.TipoVehiculo.TIPO_VEHICULO_UNSPECIFIED) {
            return null;
        }
        String name = proto.name().replace("TIPO_VEHICULO_", "");
        return TipoVehiculo.valueOf(name);
    }

    private com.rentar.vehicle.grpc.TipoVehiculo mapTipoToProto(TipoVehiculo domain) {
        if (domain == null) {
            return com.rentar.vehicle.grpc.TipoVehiculo.TIPO_VEHICULO_UNSPECIFIED;
        }
        return com.rentar.vehicle.grpc.TipoVehiculo.valueOf("TIPO_VEHICULO_" + domain.name());
    }

    private com.rentar.vehicle.grpc.EstadoVehiculo mapEstadoToProto(EstadoVehiculo domain) {
        if (domain == null) {
            return com.rentar.vehicle.grpc.EstadoVehiculo.ESTADO_VEHICULO_UNSPECIFIED;
        }
        return com.rentar.vehicle.grpc.EstadoVehiculo.valueOf("ESTADO_VEHICULO_" + domain.name());
    }

    public VehiculoExternoRequestDTO toRequestDTO(RegistrarVehiculoExternoRequest req) {
        return VehiculoExternoRequestDTO.builder()
                .patente(req.getPatente())
                .marca(req.getMarca())
                .modelo(req.getModelo())
                .anio(req.getAnio())
                .color(req.getColor())
                .tipoVehiculo(mapTipo(req.getTipoVehiculo()))
                .precioDiario(BigDecimal.valueOf(req.getPrecioDiario()))
                .documento(req.getDocumento())
                .clienteId(req.getIdCliente())
                .build();
    }

    public VehiculoExterno toProto(VehiculoExternoResponseDTO dto) {
        return VehiculoExterno.newBuilder()
                .setIdVehiculoExterno(dto.getIdVehiculoExterno() != null ? dto.getIdVehiculoExterno() : 0L)
                .setIdCliente(dto.getClienteId() != null ? dto.getClienteId() : 0L)
                .setPatente(dto.getPatente() == null ? "" : dto.getPatente())
                .setMarca(dto.getMarca() == null ? "" : dto.getMarca())
                .setModelo(dto.getModelo() == null ? "" : dto.getModelo())
                .setAnio(dto.getAnio() != null ? dto.getAnio() : 0)
                .setColor(dto.getColor() == null ? "" : dto.getColor())
                .setTipoVehiculo(mapTipoToProto(dto.getTipoVehiculo()))
                .setEstado(dto.getEstado() == null ? "" : dto.getEstado())
                .setValidacionPatente(dto.getValidacionPatente() == null ? "" : dto.getValidacionPatente())
                .setValidacionTitularidad(dto.getValidacionTitularidad() == null ? "" : dto.getValidacionTitularidad())
                .setIdVehiculo(dto.getVehiculoId() != null ? dto.getVehiculoId() : 0L)
                .setPrecioDiario(dto.getPrecioDiario() != null ? dto.getPrecioDiario().doubleValue() : 0.0)
                .build();
    }

    public List<VehiculoExterno> toProtoListExternos(List<VehiculoExternoResponseDTO> dtos) {
        return dtos.stream().map(this::toProto).collect(Collectors.toList());
    }
}