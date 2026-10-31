package com.rentar.mantenimiento_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaConfirmadaEvent {
    private Long reservaId;
    private Long vehiculoId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}
