package com.rentar.vehicle_service.grpc.validation;

import com.rentar.vehicle_service.exception.custom.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class RequestValidator {
    private final Validator validator;

    public RequestValidator(Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T request) {
        Set<ConstraintViolation<T>> violaciones = validator.validate(request);
        if (!violaciones.isEmpty()) {
            List<String> errores = violaciones.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .toList();
            throw new ValidationException(errores);
        }
    }
}
