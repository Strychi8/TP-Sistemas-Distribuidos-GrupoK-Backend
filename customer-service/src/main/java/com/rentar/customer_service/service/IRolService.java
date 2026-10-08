package com.rentar.customer_service.service;


import com.rentar.customer_service.enums.NombreRol;
import com.rentar.customer_service.model.Rol;

public interface IRolService {
    boolean existsByNombreRol(NombreRol nombreRol);
    Rol findByNombreRol(NombreRol nombreRol);
}
