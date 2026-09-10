package com.empresa_rentar.web_services.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email; // O username

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Builder.Default
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsuarioRol> roles = new ArrayList<>();
}
