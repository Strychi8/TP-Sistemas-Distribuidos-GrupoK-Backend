package com.rentar.vehicle_service.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PatenteValidator implements ConstraintValidator<Patente, String> {
    private static final String PATENTE_REGEX = "^(?:[A-Z]{3}[0-9]{3}|[A-Z]{2}[0-9]{3}[A-Z]{2})$";
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String patente = PatenteNormalizer.normalizar(value);

        return patente.matches(PATENTE_REGEX);
    }
}
