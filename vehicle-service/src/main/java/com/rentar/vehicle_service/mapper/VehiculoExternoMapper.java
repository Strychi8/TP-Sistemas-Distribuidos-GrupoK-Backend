package com.rentar.vehicle_service.mapper;

import com.rentar.vehicle_service.dto.request.VehiculoExternoRequestDTO;
import com.rentar.vehicle_service.dto.response.VehiculoExternoResponseDTO;
import com.rentar.vehicle_service.model.VehiculoExterno;
import org.springframework.stereotype.Component;

@Component
public class VehiculoExternoMapper {

    public VehiculoExterno toEntity(VehiculoExternoRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return VehiculoExterno.builder()
                .clienteId(dto.getClienteId())
                .patente(dto.getPatente())
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .anio(dto.getAnio())
                .color(dto.getColor())
                .tipoVehiculo(dto.getTipoVehiculo())
                .precioDiario(dto.getPrecioDiario())
                .estado("PENDIENTE")
                .validacionPatente("PENDIENTE")
                .validacionTitularidad("PENDIENTE")
                .build();
    }

    public VehiculoExternoResponseDTO toDTO(VehiculoExterno entity) {
        if (entity == null) {
            return null;
        }

        return VehiculoExternoResponseDTO.builder()
                .idVehiculoExterno(entity.getIdVehiculoExterno())
                .clienteId(entity.getClienteId())
                .patente(entity.getPatente())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .anio(entity.getAnio())
                .color(entity.getColor())
                .tipoVehiculo(entity.getTipoVehiculo())
                .estado(entity.getEstado())
                .validacionPatente(entity.getValidacionPatente())
                .validacionTitularidad(entity.getValidacionTitularidad())
                .vehiculoId(entity.getVehiculoId())
                .precioDiario(entity.getPrecioDiario())
                .build();
    }
}
