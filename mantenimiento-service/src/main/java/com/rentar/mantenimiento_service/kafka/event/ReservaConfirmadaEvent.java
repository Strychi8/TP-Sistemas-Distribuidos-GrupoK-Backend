package com.rentar.mantenimiento_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento de integración que informa que una reserva fue confirmada.
 *
 * <p>El microservicio de mantenimiento utiliza estos datos para crear una
 * solicitud preventiva sin bloquear el flujo principal de la reserva.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaConfirmadaEvent {
    private Long reservaId;
    private Long vehiculoId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}
