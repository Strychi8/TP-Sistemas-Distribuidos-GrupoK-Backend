package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.model.Usuario;

public interface IUsuarioService {
    Usuario crearUsuario(String email, String password);
}