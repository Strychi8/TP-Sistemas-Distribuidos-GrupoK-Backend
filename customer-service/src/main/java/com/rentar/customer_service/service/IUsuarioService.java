package com.rentar.customer_service.service;

import com.rentar.customer_service.model.Usuario;

public interface IUsuarioService {
    Usuario crearUsuario(String email, String password);
    void actualizarUsuario(Usuario usuario, String email, String password);
}