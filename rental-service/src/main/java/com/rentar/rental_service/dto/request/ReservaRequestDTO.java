package com.rentar.rental_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservaRequestDTO(
        @NotNull(message = "El ID cliente es obligatorio")
        Long idCliente,

        @NotNull(message = "El ID vehículo es obligatorio")
        Long idVehiculo,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @Future(message = "La fecha de inicio debe ser futura")
        LocalDateTime fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        @Future(message = "La fecha de fin debe ser futura")
        LocalDateTime fechaFin,

        @NotNull(message = "El precio diario es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio diario debe ser mayor a cero")
        BigDecimal precioDiario
) {
}
