package com.rentar.mantenimiento_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Evento interno de Spring que conecta el servicio con el productor Kafka. */
@AllArgsConstructor
@Getter
public class MantenimientoCompletadoInternalEvent {
    /** Identificador de la reserva asociada. */
    private final Long reservaId;
    /** Identificador del vehículo asociado. */
    private final Long vehiculoId;
    /** Resultado guardado: REALIZADO o NO_REALIZADO. */
    private final String resultado;
}
