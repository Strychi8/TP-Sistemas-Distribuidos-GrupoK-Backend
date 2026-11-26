package com.rentar.mantenimiento_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Mensaje Kafka con el resultado del mantenimiento asociado a una reserva. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MantenimientoResultadoEvent {
    /** Identificador de la reserva. */
    private Long reservaId;
    /** Identificador del vehículo. */
    private Long vehiculoId;
    /** Resultado: REALIZADO o NO_REALIZADO. */
    private String resultado;
}
