package com.rentar.vehicle_service.dto.response;

import com.rentar.vehicle_service.enums.EstadoVehiculo;
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
public class VehiculoResponseDTO {

    private Long idVehiculo;
    private String patente;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private TipoVehiculo tipoVehiculo;
    private BigDecimal precioDiario;
    private EstadoVehiculo estado;
    private Boolean activo;
}
