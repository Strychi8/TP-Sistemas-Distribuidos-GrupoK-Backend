package com.rentar.mantenimiento_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de integración que comunica el resultado del mantenimiento
 * preventivo asociado a una reserva.
 *
 * <p>El resultado permite a otros componentes conocer si el mantenimiento
 * fue realizado o no realizado.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MantenimientoResultadoEvent {
    private Long reservaId;
    private Long vehiculoId;
    private String resultado; // "REALIZADO" o "NO_REALIZADO"
}
