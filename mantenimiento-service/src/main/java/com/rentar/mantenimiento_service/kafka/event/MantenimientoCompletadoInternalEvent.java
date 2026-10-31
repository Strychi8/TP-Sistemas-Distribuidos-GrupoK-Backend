package com.rentar.mantenimiento_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Evento interno que representa el cierre transaccional de una solicitud de
 * mantenimiento.
 *
 * <p>Se publica dentro de la aplicación después de actualizar la solicitud y
 * sirve como puente para emitir el evento de resultado hacia Kafka.</p>
 */
@AllArgsConstructor
@Getter
public class MantenimientoCompletadoInternalEvent {
    private final Long reservaId;
    private final Long vehiculoId;
    private final String resultado; // "REALIZADO" o "NO_REALIZADO"
}
