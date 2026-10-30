package com.rentar.vehicle_service.dto.response;

import com.rentar.vehicle_service.enums.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoExternoResponseDTO {
    private Long idVehiculoExterno;
    private Long clienteId;
    private String patente;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private TipoVehiculo tipoVehiculo;
    private String estado;
    private String validacionPatente;
    private String validacionTitularidad;
    private Long vehiculoId;
    private BigDecimal precioDiario;
}
