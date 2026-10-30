package com.rentar.vehicle_service.kafka.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerificacionResultadoEvent {
    private Long vehiculoId;
    private String patente;
    private String resultado;
}
