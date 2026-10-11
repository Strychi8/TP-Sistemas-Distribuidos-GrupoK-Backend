package com.rentar.vehicle_service.exception.custom;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<String> errores;
    public ValidationException(List<String> errores) {
        super(String.join("; ", errores));
        this.errores = List.copyOf(errores);
    }
    public List<String> getErrores() {
        return errores;
    }
}
