package com.empresa_rentar.web_services.service;


import com.empresa_rentar.web_services.model.Rol;

public interface IRolService {
    boolean existsByNombreRol(String nombreRol);
    Rol findByNombreRol(String nombreRol);
}
