package com.rentar.vehicle_service.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PatenteValidatorTest {

    private PatenteValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PatenteValidator();
    }

    @Test
    @DisplayName("Patentes formato tradicional (ABC123) y Mercosur (AB123CD) son válidas")
    void testPatentesValidas() {
        assertTrue(validator.isValid("ABC123", null), "Formato tradicional debe ser válido");
        assertTrue(validator.isValid("AB123CD", null), "Formato Mercosur debe ser válido");
    }

    @Test
    @DisplayName("Patente con minúsculas y espacios se normaliza y es válida")
    void testNormalizacionEspaciosYMinusculas() {
        assertEquals("AB123CD", PatenteNormalizer.normalizar("ab 123 cd"));
        assertTrue(validator.isValid("ab 123 cd", null), "'ab 123 cd' debe ser aceptado tras normalizar");
        assertTrue(validator.isValid("ab-123-cd", null), "'ab-123-cd' con guiones debe ser aceptado");
    }

    @ParameterizedTest
    @ValueSource(strings = {"A1234", "ABCD12", "123ABC", "ABC12", "AB1234C", "INVALIDA"})
    @DisplayName("Patentes con formatos inválidos son rechazadas")
    void testPatentesInvalidas(String patenteInvalida) {
        assertFalse(validator.isValid(patenteInvalida, null), "Patente " + patenteInvalida + " debe ser rechazada");
    }
}
