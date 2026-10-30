package com.rentar.vehicle_service.dto.request;

import com.rentar.vehicle_service.enums.TipoVehiculo;
import com.rentar.vehicle_service.validation.Patente;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoExternoRequestDTO {

    @NotBlank(message = "La patente es obligatoria")
    @Patente
    private String patente;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotNull(message = "El año es obligatorio")
    @Min(value = 1900, message = "El año no es válido")
    private Integer anio;

    private String color;

    @NotNull(message = "El tipo de vehículo es obligatorio")
    private TipoVehiculo tipoVehiculo;

    @NotNull(message = "El precio diario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio diario debe ser mayor a 0")
    private BigDecimal precioDiario;

    @NotBlank(message = "El documento es obligatorio")
    private String documento;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;
}
