package com.rentar.verificacion_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una entrada del registro simulado de titularidad vehicular.
 * El servicio compara estos datos con el cliente que postula un vehículo.
 */
@Entity
@Table(name = "titularidades_vehiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitularidadVehiculo {

    /** Identificador de la entrada del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador del cliente asociado al titular. */
    @Column(name = "cliente_id")
    private Long clienteId;

    /** Documento de identidad registrado para el titular. */
    @Column(length = 20)
    private String documento;

    /** Patente del vehículo registrado. */
    @Column(nullable = false, length = 20)
    private String patente;

    /** Nombre del titular en el registro simulado. */
    @Column(name = "nombre_titular", length = 100)
    private String nombreTitular;
}
