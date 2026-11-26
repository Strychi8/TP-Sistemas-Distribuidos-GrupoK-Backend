package com.rentar.verificacion_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultado de consultar una patente en el registro policial simulado.
 * Se publica en el topic vehiculos.verificacion.resultado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionResultadoEvent {
    /** Identificador del vehículo de la flota. */
    private Long vehiculoId;
    /** Patente consultada. */
    private String patente;
    /** Resultado de la consulta: VALIDADO o ROBADO. */
    private String resultado;
}
