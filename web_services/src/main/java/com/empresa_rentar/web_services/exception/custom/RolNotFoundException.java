package com.empresa_rentar.web_services.exception.custom;

public class RolNotFoundException extends RuntimeException {
    public RolNotFoundException() {
        super("Rol no encontrado");
    }
}
