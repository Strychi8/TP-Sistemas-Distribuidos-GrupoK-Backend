package com.rentar.verificacion_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Evento publicado al topico 'vehiculos.verificacion.resultado'
// Informa el resultado del chequeo policial: 'VALIDADO' o 'ROBADO'
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionResultadoEvent {
    private Long vehiculoId;
    private String patente;
    private String resultado; // "VALIDADO" o "ROBADO"
}
