package com.rentar.vehicle_service.kafka.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerificacionVehiculoEvent {
    private Long vehiculoId;
    private String patente;
}
