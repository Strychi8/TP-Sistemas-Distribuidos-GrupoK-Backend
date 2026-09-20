package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.model.Usuario;

import java.util.Optional;

public interface IUsuarioService {
    Usuario crearUsuario(String email, String password);
    void actualizarUsuario(Usuario usuario, String email, String password);
    Optional<Usuario> findByEmail(String email);
}