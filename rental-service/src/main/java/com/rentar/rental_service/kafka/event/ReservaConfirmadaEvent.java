package com.rentar.rental_service.kafka.event;

import java.time.LocalDateTime;

public record ReservaConfirmadaEvent(
        Long reservaId,
        Long vehiculoId,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {
}