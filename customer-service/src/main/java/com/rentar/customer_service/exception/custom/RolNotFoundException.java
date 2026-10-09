package com.rentar.customer_service.exception.custom;

public class RolNotFoundException extends RuntimeException {
    public RolNotFoundException() {
        super("Rol no encontrado");
    }
}
