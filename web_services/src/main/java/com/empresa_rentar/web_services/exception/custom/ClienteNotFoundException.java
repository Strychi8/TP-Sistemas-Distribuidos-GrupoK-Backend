package com.empresa_rentar.web_services.exception.custom;

public class ClienteNotFoundException extends RuntimeException {
    public ClienteNotFoundException() {
        super("Cliente no encontrado");
    }
}
