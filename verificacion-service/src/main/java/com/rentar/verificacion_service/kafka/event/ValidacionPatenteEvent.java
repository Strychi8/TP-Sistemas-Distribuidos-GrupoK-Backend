package com.rentar.verificacion_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultado de validar la patente de un vehículo externo.
 * Se publica en el topic vehiculos.externos.validacion.patente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidacionPatenteEvent {
    /** Identificador de la solicitud de incorporación del vehículo. */
    private Long vehiculoExternoId;
    /** Patente consultada en el registro policial simulado. */
    private String patente;
    /** Resultado de la validación: VALIDADO o RECHAZADO. */
    private String resultado;
}
