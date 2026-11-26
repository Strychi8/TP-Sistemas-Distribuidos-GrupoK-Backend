package com.rentar.verificacion_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultado de comparar al cliente solicitante con el registro de titularidad.
 * Se publica en el topic vehiculos.externos.validacion.titularidad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidacionTitularidadEvent {
    /** Identificador de la solicitud de incorporación del vehículo. */
    private Long vehiculoExternoId;
    /** Identificador del cliente que presentó la solicitud. */
    private Long clienteId;
    /** Documento usado para comprobar la titularidad. */
    private String documento;
    /** Nombre del cliente. */
    private String nombre;
    /** Apellido del cliente. */
    private String apellido;
    /** Email del cliente. */
    private String email;
    /** Patente consultada en el registro de titularidad. */
    private String patente;
    /** Resultado de la validación: VALIDADO o RECHAZADO. */
    private String resultado;
}
