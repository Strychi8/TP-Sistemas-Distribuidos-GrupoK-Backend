package com.rentar.verificacion_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud de verificación policial para un vehículo recién registrado.
 * Se consume desde el topic vehiculos.verificacion.solicitada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionVehiculoEvent {
    /** Identificador del vehículo de la flota. */
    private Long vehiculoId;
    /** Patente que se consultará en el registro policial simulado. */
    private String patente;
}
