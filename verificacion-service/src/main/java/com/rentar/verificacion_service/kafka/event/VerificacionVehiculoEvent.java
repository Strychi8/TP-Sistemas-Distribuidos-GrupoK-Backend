package com.rentar.verificacion_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Evento consumido desde el topico 'vehiculos.verificacion.solicitada'
// Disparado al dar de alta un vehiculo en la flota
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionVehiculoEvent {
    private Long vehiculoId;
    private String patente;
}
