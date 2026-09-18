package com.empresa_rentar.web_services.dto.request;

import com.empresa_rentar.web_services.enums.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class VehiculoDisponibilidadRequestDTO {
    private TipoVehiculo tipoVehiculo;
    private String marca;
    private String modelo;
    // Se declara como Double porque GraphQL mapea Float a Double
    private Double precioMin;
    private Double precioMax;
}
