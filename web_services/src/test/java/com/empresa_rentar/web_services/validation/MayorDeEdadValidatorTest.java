package com.empresa_rentar.web_services.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MayorDeEdadValidator - tests unitarios")
class MayorDeEdadValidatorTest {

    private final MayorDeEdadValidator validator = new MayorDeEdadValidator();

    @Test
    @DisplayName("devuelve true cuando la fecha es null (la obligatoriedad la valida @NotNull)")
    void deberiaSerValidoCuandoLaFechaEsNull() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("devuelve true cuando la persona cumple 18 anios exactamente hoy")
    void deberiaSerValidoCuandoCumple18AniosHoy() {
        LocalDate hoyCumple18 = LocalDate.now().minusYears(18);

        assertThat(validator.isValid(hoyCumple18, null)).isTrue();
    }

    @Test
    @DisplayName("devuelve true cuando la persona tiene mas de 18 anios")
    void deberiaSerValidoCuandoTieneMasDe18Anios() {
        LocalDate mayoriaDeEdadCumplida = LocalDate.now().minusYears(30);

        assertThat(validator.isValid(mayoriaDeEdadCumplida, null)).isTrue();
    }

    @Test
    @DisplayName("devuelve false cuando falta un dia para cumplir 18 anios")
    void deberiaSerInvalidoCuandoTiene17AniosY364Dias() {
        LocalDate unDiaAntesDeCumplir18 = LocalDate.now().minusYears(18).plusDays(1);

        assertThat(validator.isValid(unDiaAntesDeCumplir18, null)).isFalse();
    }

    @Test
    @DisplayName("devuelve false cuando la fecha de nacimiento es futura")
    void deberiaSerInvalidoCuandoLaFechaEsFutura() {
        LocalDate manana = LocalDate.now().plusDays(1);

        assertThat(validator.isValid(manana, null)).isFalse();
    }
}
