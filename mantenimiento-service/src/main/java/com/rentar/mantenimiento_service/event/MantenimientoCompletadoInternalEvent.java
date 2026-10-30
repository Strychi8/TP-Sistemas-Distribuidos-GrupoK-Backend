package com.rentar.mantenimiento_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MantenimientoCompletadoInternalEvent {
    private final Long reservaId;
    private final Long vehiculoId;
    private final String resultado; // "REALIZADO" o "NO_REALIZADO"

}
