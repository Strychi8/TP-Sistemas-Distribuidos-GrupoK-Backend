package com.empresa_rentar.web_services.model;

import com.empresa_rentar.web_services.enums.EstadoVehiculo;
import com.empresa_rentar.web_services.enums.TipoVehiculo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVehiculo;

    @Column(name = "patente", nullable = false, unique = true, updatable = false, length = 15)
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

    @Column(name = "precio_diario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioDiario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
