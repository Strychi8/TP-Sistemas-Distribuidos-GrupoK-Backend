package com.rentar.mantenimiento_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MantenimientoResultadoEvent {
    private Long reservaId;
    private Long vehiculoId;
    private String resultado; // "REALIZADO" o "NO_REALIZADO"
}
