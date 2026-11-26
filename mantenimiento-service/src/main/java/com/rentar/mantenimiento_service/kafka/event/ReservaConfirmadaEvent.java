package com.rentar.mantenimiento_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Evento recibido cuando el sistema confirma una reserva. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaConfirmadaEvent {
    /** Identificador de la reserva confirmada. */
    private Long reservaId;
    /** Identificador del vehículo reservado. */
    private Long vehiculoId;
    /** Inicio del período de reserva. */
    private LocalDateTime fechaInicio;
    /** Fin del período de reserva. */
    private LocalDateTime fechaFin;
}
