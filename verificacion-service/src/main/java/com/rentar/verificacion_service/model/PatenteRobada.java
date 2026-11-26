package com.rentar.verificacion_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Representa una patente incluida en el registro policial simulado. */
@Entity
@Table(name = "patentes_robadas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatenteRobada {

    /** Identificador del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Patente denunciada. No admite duplicados ni valores nulos. */
    @Column(nullable = false, unique = true, length = 20)
    private String patente;

    /** Descripción del motivo de la denuncia o restricción. */
    @Column(length = 255)
    private String motivo;

    /** Fecha de la denuncia; se asigna al insertar si no se informó. */
    @Column(name = "fecha_denuncia")
    private LocalDateTime fechaDenuncia;

    /** Asigna la fecha y hora actuales antes de insertar el registro. */
    @PrePersist
    public void prePersist() {
        if (fechaDenuncia == null) {
            fechaDenuncia = LocalDateTime.now();
        }
    }
}
