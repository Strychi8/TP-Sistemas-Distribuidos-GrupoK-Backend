package com.rentar.rental_service.kafka.event;

public record MantenimientoResultadoEvent(
        Long reservaId,
        Long vehiculoId,
        String resultado
) {
}