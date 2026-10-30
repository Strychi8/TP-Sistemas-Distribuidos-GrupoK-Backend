package com.rentar.rental_service.dto.response;

import com.rentar.rental_service.enums.EstadoReserva;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservaResponseDTO(
        Long idReserva,
        Long idCliente,
        Long idVehiculo,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        BigDecimal precioDiario,
        BigDecimal importeTotal,
        EstadoReserva estadoReserva,
        String resultadoMantenimiento
) {
}
