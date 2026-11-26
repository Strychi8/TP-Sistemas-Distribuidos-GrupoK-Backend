package com.rentar.mantenimiento_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Representa la solicitud de mantenimiento asociada a una reserva. */
@Entity
@Table(name = "solicitud_mantenimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudMantenimiento {

    /** Identificador de la solicitud. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador único de reserva; evita crear solicitudes duplicadas. */
    @Column(name = "reserva_id", nullable = false, unique = true)
    private Long reservaId;

    /** Identificador del vehículo asociado a la reserva. */
    @Column(name = "vehiculo_id", nullable = false)
    private Long vehiculoId;

    /** Inicio del período de la reserva. */
    private LocalDateTime fechaInicio;

    /** Fin del período de la reserva. */
    private LocalDateTime fechaFin;

    /** Estado del mantenimiento; se guarda como texto y comienza en PENDIENTE. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoMantenimiento estado = EstadoMantenimiento.PENDIENTE;

    /** Fecha y hora en que se creó la solicitud. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

}
