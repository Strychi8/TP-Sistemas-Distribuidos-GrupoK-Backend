package com.rentar.vehicle_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionVehiculoEvent {
    private Long vehiculoId;
    private String patente;
}
