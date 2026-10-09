package com.rentar.customer_service.exception.custom;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(){
        super("El email ya está asignado a otro usuario");
    }
}
