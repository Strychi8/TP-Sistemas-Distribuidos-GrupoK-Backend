package com.rentar.vehicle_service.model;

import com.rentar.vehicle_service.enums.TipoVehiculo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehiculos_externos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoExterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVehiculoExterno;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "patente", nullable = false, length = 15)
    private String patente;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 50)
    private String modelo;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "color", length = 30)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false, length = 30)
    private TipoVehiculo tipoVehiculo;

    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(name = "validacion_patente", nullable = false, length = 30)
    @Builder.Default
    private String validacionPatente = "PENDIENTE";

    @Column(name = "validacion_titularidad", nullable = false, length = 30)
    @Builder.Default
    private String validacionTitularidad = "PENDIENTE";

    @Column(name = "vehiculo_id")
    private Long vehiculoId;

    @Column(name = "precio_diario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioDiario;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.fechaCreacion == null) {
            this.fechaCreacion = now;
        }
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = now;
        }
        if (this.estado == null) {
            this.estado = "PENDIENTE";
        }
        if (this.validacionPatente == null) {
            this.validacionPatente = "PENDIENTE";
        }
        if (this.validacionTitularidad == null) {
            this.validacionTitularidad = "PENDIENTE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
