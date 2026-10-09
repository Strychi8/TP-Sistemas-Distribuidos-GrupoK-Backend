package com.rentar.customer_service.exception.custom;

public class DniAlreadyExistsException extends RuntimeException {
    public DniAlreadyExistsException() {
        super("El DNI ya se encuentra registrado");
    }
}
