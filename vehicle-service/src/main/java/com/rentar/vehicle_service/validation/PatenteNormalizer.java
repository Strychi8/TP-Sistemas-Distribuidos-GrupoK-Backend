package com.rentar.vehicle_service.validation;

import lombok.AllArgsConstructor;

import java.util.Locale;

public final class PatenteNormalizer {
    private static final String SEPARADORES = "[\\s-]";

    private PatenteNormalizer() {}

    public static String normalizar(String patente) {
        if (patente == null) {
            return null;
        }
        return patente.trim().replaceAll(SEPARADORES, "").toUpperCase(Locale.ROOT);
    }

}
