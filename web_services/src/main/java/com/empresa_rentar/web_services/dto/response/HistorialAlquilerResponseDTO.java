package com.empresa_rentar.web_services.dto.response;

import com.empresa_rentar.web_services.enums.EstadoReserva;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Registro del historial de alquileres de un cliente")
public record HistorialAlquilerResponseDTO(

        @Schema(description = "Marca y modelo del vehículo", example = "Toyota Corolla")
        String vehiculo,

        @Schema(description = "Patente del vehículo", example = "ABC123BC")
        String patente,

        @Schema(description = "Fecha y hora de inicio del alquiler", example = "2026-09-15T10:00:00")
        String fechaInicio,

        @Schema(description = "Fecha y hora de finalización del alquiler", example = "2026-09-20T10:00:00")
        String fechaFinalizacion,

        @Schema(description = "Cantidad de días del alquiler", example = "5")
        Long cantidadDias,

        @Schema(description = "Importe total del alquiler", example = "225000.00")
        BigDecimal importeTotal,

        @Schema(description = "Estado de la reserva", example = "FINALIZADA")
        EstadoReserva estado

) {
}