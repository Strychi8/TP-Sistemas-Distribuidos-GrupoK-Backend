package com.empresa_rentar.web_services.exception.custom;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(){
        super("El email ya está asignado a otro usuario");
    }
}
