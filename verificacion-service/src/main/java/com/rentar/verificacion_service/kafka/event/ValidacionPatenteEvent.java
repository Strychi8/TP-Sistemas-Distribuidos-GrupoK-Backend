package com.rentar.verificacion_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Evento publicado al topico 'vehiculos.externos.validacion.patente'
// Resultado de validar que la patente externa no este denunciada como robada: 'VALIDADO' o 'RECHAZADO'
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidacionPatenteEvent {
    private Long vehiculoExternoId;
    private String patente;
    private String resultado; // "VALIDADO" o "RECHAZADO"
}
