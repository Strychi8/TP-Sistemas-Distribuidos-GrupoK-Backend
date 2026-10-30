package com.rentar.verificacion_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Entidad que simula el registro policial de vehiculos denunciados como robados
@Entity
@Table(name = "patentes_robadas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatenteRobada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String patente;

    @Column(length = 255)
    private String motivo;

    @Column(name = "fecha_denuncia")
    private LocalDateTime fechaDenuncia;

    @PrePersist
    public void prePersist() {
        if (fechaDenuncia == null) {
            fechaDenuncia = LocalDateTime.now();
        }
    }
}
