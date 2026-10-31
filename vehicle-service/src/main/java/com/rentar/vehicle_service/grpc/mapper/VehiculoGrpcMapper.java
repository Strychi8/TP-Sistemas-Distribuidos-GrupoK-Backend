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

    public FiltroDisponibilidadDTO toFiltroDTO(FiltroDisponibilidad proto) {
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

    public VehicleList toProtoList(List<VehiculoResponseDTO> dtos) {
        VehicleList.Builder builder = VehicleList.newBuilder();
        dtos.forEach(dto -> builder.addVehicles(toProto(dto)));
        return builder.build();
    }

    // ---------- Enums: mismos nombres, tipos Java distintos ----------
    // Los valores del proto coinciden con el dominio, PERO el valor 0 (UNSPECIFIED)
    // no tiene contraparte -> se mapea a null y luego se rechaza como INVALID_ARGUMENT.

    public EstadoVehiculo toEstado(com.rentar.vehicle.grpc.EstadoVehiculo proto) {
        if (proto == null || proto == com.rentar.vehicle.grpc.EstadoVehiculo.ESTADO_VEHICULO_UNSPECIFIED) {
            return null;
        }
        return EstadoVehiculo.valueOf(proto.name());
    }

    private TipoVehiculo mapTipo(com.rentar.vehicle.grpc.TipoVehiculo proto) {
        if (proto == null || proto == com.rentar.vehicle.grpc.TipoVehiculo.TIPO_VEHICULO_UNSPECIFIED) {
            return null;
        }
        return TipoVehiculo.valueOf(proto.name());
    }

    private com.rentar.vehicle.grpc.TipoVehiculo mapTipoToProto(TipoVehiculo domain) {
        if (domain == null) {
            return com.rentar.vehicle.grpc.TipoVehiculo.TIPO_VEHICULO_UNSPECIFIED;
        }
        return com.rentar.vehicle.grpc.TipoVehiculo.valueOf(domain.name());
    }

    private com.rentar.vehicle.grpc.EstadoVehiculo mapEstadoToProto(EstadoVehiculo domain) {
        if (domain == null) {
            return com.rentar.vehicle.grpc.EstadoVehiculo.ESTADO_VEHICULO_UNSPECIFIED;
        }
        return com.rentar.vehicle.grpc.EstadoVehiculo.valueOf(domain.name());
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
                .clienteId(req.getClienteId())
                .build();
    }

    public VehiculoExternoResponse toProto(VehiculoExternoResponseDTO dto) {
        return VehiculoExternoResponse.newBuilder()
                .setIdVehiculoExterno(dto.getIdVehiculoExterno() != null ? dto.getIdVehiculoExterno() : 0L)
                .setClienteId(dto.getClienteId() != null ? dto.getClienteId() : 0L)
                .setPatente(dto.getPatente() == null ? "" : dto.getPatente())
                .setMarca(dto.getMarca() == null ? "" : dto.getMarca())
                .setModelo(dto.getModelo() == null ? "" : dto.getModelo())
                .setAnio(dto.getAnio() != null ? dto.getAnio() : 0)
                .setColor(dto.getColor() == null ? "" : dto.getColor())
                .setTipoVehiculo(mapTipoToProto(dto.getTipoVehiculo()))
                .setEstado(dto.getEstado() == null ? "" : dto.getEstado())
                .setValidacionPatente(dto.getValidacionPatente() == null ? "" : dto.getValidacionPatente())
                .setValidacionTitularidad(dto.getValidacionTitularidad() == null ? "" : dto.getValidacionTitularidad())
                .setVehiculoId(dto.getVehiculoId() != null ? dto.getVehiculoId() : 0L)
                .setPrecioDiario(dto.getPrecioDiario() != null ? dto.getPrecioDiario().doubleValue() : 0.0)
                .build();
    }

    public VehiculoExternoList toProtoListExternos(List<VehiculoExternoResponseDTO> dtos) {
        VehiculoExternoList.Builder builder = VehiculoExternoList.newBuilder();
        dtos.forEach(dto -> builder.addVehiculosExternos(toProto(dto)));
        return builder.build();
    }
}