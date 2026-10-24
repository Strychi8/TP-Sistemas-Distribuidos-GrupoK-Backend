package com.rentar.verificacion_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entidad que simula el registro externo de titularidades vehiculares (ej: DNRPA)
// Permite verificar si un cliente es el titular registral del vehiculo que desea incorporar
@Entity
@Table(name = "titularidades_vehiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitularidadVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(length = 20)
    private String documento;

    @Column(nullable = false, length = 20)
    private String patente;

    @Column(name = "nombre_titular", length = 100)
    private String nombreTitular;
}
