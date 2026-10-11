package com.rentar.vehicle_service.dto.request;

import com.rentar.vehicle_service.enums.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroDisponibilidadDTO {
    private TipoVehiculo tipoVehiculo;
    private String marca;
    private String modelo;
    private Double precioMin;
    private Double precioMax;
}
