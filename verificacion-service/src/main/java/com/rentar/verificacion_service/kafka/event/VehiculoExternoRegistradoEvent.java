package com.rentar.verificacion_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Evento consumido desde el topico 'vehiculos.externos.registrados'
// Disparado cuando un cliente registra un vehiculo de su propiedad
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
