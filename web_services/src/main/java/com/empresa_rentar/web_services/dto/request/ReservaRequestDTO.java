package com.empresa_rentar.web_services.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "objeto de transferencia para la creación de una reserva")
public record ReservaRequestDTO(

        @Schema(description = "ID del cliente que realiza la reserva", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El ID cliente es obligatorio")
        Long idCliente,

        @Schema(description = "ID del vehículo seleccionado para el alquiler", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El ID vehículo es obligatorio")
        Long idVehiculo,

        @Schema(description = "Fecha y hora de inicio del alquiler (debe ser futura)", example = "2026-10-01T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La fecha de inicio es obligatoria")
        @Future(message = "La fecha de inicio debe ser futura")
        LocalDateTime fechaInicio,

        @Schema(description = "Fecha y hora de finalización del alquiler", example = "2026-10-05T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDateTime fechaFin
) {
}
