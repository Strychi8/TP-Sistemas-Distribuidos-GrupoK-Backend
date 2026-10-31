package com.rentar.mantenimiento_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa la solicitud de mantenimiento preventivo generada para una
 * reserva confirmada.
 *
 * <p>La reserva identifica de forma única la solicitud, mientras que el
 * vehículo, el período reservado y el estado permiten registrar y consultar
 * el ciclo completo del mantenimiento.</p>
 */
@Entity
@Table(name = "solicitud_mantenimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // REQUISITO CLAVE: Clave única para garantizar Idempotencia
    @Column(name = "reserva_id", nullable = false, unique = true)
    private Long reservaId;

    @Column(name = "vehiculo_id", nullable = false)
    private Long vehiculoId;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoMantenimiento estado = EstadoMantenimiento.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

}
