package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.model.Usuario;

public interface IUsuarioRolService {
    void asignarRol(Usuario usuario, String nombreRol);
}
