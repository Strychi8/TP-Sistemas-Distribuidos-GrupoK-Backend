package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.enums.NombreRol;
import com.empresa_rentar.web_services.model.Usuario;

import java.util.List;

public interface IUsuarioRolService {
    void asignarRol(Usuario usuario, NombreRol nombreRol);
    List<NombreRol> obtenerRolesDeUsuario(Long idUsuario);
}
