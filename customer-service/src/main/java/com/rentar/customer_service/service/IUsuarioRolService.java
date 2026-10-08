package com.rentar.customer_service.service;

import com.rentar.customer_service.enums.NombreRol;
import com.rentar.customer_service.model.Usuario;

public interface IUsuarioRolService {
    void asignarRol(Usuario usuario, NombreRol nombreRol);
}
