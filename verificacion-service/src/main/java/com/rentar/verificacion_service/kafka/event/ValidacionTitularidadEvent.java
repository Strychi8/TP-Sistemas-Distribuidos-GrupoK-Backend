package com.rentar.verificacion_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Evento publicado al topico 'vehiculos.externos.validacion.titularidad'
// Resultado de validar que el cliente solicitante sea el titular del vehiculo: 'VALIDADO' o 'RECHAZADO'
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidacionTitularidadEvent {
    private Long vehiculoExternoId;
    private Long clienteId;
    private String documento;
    private String patente;
    private String resultado; // "VALIDADO" o "RECHAZADO"
}
