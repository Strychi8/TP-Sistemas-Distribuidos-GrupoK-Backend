package com.rentar.vehicle_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoExternoRegistradoEvent {
    private Long vehiculoExternoId;
    private Long clienteId;
    private String documento;
    private String patente;
}
