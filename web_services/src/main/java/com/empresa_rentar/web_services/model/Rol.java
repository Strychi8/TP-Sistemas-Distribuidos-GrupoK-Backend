package com.empresa_rentar.web_services.model;

import com.empresa_rentar.web_services.enums.NombreRol;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private NombreRol nombreRol;
}
