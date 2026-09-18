package com.empresa_rentar.web_services.service;


import com.empresa_rentar.web_services.enums.NombreRol;
import com.empresa_rentar.web_services.model.Rol;

public interface IRolService {
    boolean existsByNombreRol(NombreRol nombreRol);
    Rol findByNombreRol(NombreRol nombreRol);
}
