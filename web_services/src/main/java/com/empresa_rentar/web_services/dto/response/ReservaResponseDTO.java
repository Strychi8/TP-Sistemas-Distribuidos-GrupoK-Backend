package com.empresa_rentar.web_services.dto.response;

import com.empresa_rentar.web_services.enums.EstadoReserva;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Respuesta detallada de una reserva registrada")
public record ReservaResponseDTO(
        @Schema(description = "Identificador único de la reserva", example = "40")
        Long idReserva,

        @Schema(description = "ID del cliente", example = "10")
        Long idCliente,

        @Schema(description = "Nombre completo del cliente", example = "Diego Fernandez")
        String nombreCliente,

        @Schema(description = "ID del vehículo", example = "5")
        Long idVehiculo,

        @Schema(description = "Patente del vehículo", example = "ABC123BC")
        String patenteVehiculo,

        @Schema(description = "Marca y modelo del vehículo", example = "Toyota Corolla")
        String modeloVehiculo,

        @Schema(description = "Fecha y hora de inicio del alquiler", example = "2026-10-01T10:00:00")
        LocalDateTime fechaInicio,

        @Schema(description = "Fecha y hora de fin de alquiler", example = "2026-10-05T10:00:00")
        LocalDateTime fechaFin,

        @Schema(description = "Precio diario aplicado al momento de reservar", example = "45000.00")
        BigDecimal precioDiario,

        @Schema(description = "Importe total calculado (días x precio diario)", example = "180000.00")
        BigDecimal importeTotal,

        @Schema(description = "Estado actual de la reserva", example = "CONFIRMADA")
        EstadoReserva estadoReserva
) {
}
