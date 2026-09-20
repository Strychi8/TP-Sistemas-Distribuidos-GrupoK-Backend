package com.empresa_rentar.web_services.mapper;

import com.empresa_rentar.web_services.dto.request.VehiculoRequestDTO;
import com.empresa_rentar.web_services.dto.request.VehiculoUpdateRequestDTO;
import com.empresa_rentar.web_services.dto.response.VehiculoResponseDTO;
import com.empresa_rentar.web_services.enums.EstadoVehiculo;
import com.empresa_rentar.web_services.model.Vehiculo;
import org.springframework.stereotype.Component;

@Component
public class VehiculoMapper {

    public Vehiculo toEntity(VehiculoRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Vehiculo.builder()
                .patente(dto.getPatente())
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .anio(dto.getAnio())
                .color(dto.getColor())
                .tipoVehiculo(dto.getTipoVehiculo())
                .precioDiario(dto.getPrecioDiario())
                .estado(EstadoVehiculo.DISPONIBLE) // Estado inicial por defecto según enunciado
                .activo(true)
                .build();
    }

    public VehiculoResponseDTO toDTO(Vehiculo entity) {
        if (entity == null) {
            return null;
        }

        return VehiculoResponseDTO.builder()
                .idVehiculo(entity.getIdVehiculo())
                .patente(entity.getPatente())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .anio(entity.getAnio())
                .color(entity.getColor())
                .tipoVehiculo(entity.getTipoVehiculo())
                .precioDiario(entity.getPrecioDiario())
                .estado(entity.getEstado())
                .activo(entity.getActivo())
                .build();
    }

    public void updateEntityFromDTO(VehiculoUpdateRequestDTO dto, Vehiculo entity) {
        if (dto == null || entity == null) {
            return;
        }

        // La patente NO se actualiza
        entity.setMarca(dto.getMarca());
        entity.setModelo(dto.getModelo());
        entity.setAnio(dto.getAnio());
        entity.setColor(dto.getColor());
        entity.setTipoVehiculo(dto.getTipoVehiculo());
        entity.setPrecioDiario(dto.getPrecioDiario());
        entity.setEstado(dto.getEstado());
    }
}
