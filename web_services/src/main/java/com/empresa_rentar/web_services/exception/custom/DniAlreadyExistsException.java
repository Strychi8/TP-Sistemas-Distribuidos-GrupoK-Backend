package com.empresa_rentar.web_services.exception.custom;

public class DniAlreadyExistsException extends RuntimeException {
    public DniAlreadyExistsException() {
        super("El DNI ya se encuentra registrado");
    }
}
