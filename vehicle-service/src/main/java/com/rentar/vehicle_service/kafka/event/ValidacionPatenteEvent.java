package com.rentar.vehicle_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidacionPatenteEvent {
    private Long vehiculoExternoId;
    private String patente;
    private String resultado;
}
