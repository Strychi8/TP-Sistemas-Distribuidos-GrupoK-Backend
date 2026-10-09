package com.rentar.customer_service.exception.custom;

public class ClienteNotFoundException extends RuntimeException {
    public ClienteNotFoundException() {
        super("Cliente no encontrado");
    }
}
