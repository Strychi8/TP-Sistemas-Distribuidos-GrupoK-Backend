package com.rentar.verificacion_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento publicado cuando un cliente registra un vehículo externo.
 * El consumidor usa estos datos para validar la patente y la titularidad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoExternoRegistradoEvent {
    /** Identificador de la solicitud de incorporación. */
    private Long vehiculoExternoId;
    /** Identificador del cliente que presentó la solicitud. */
    private Long clienteId;
    /** Documento asociado al cliente para validar la titularidad. */
    private String documento;
    /** Patente que se consulta en los registros simulados. */
    private String patente;
}
