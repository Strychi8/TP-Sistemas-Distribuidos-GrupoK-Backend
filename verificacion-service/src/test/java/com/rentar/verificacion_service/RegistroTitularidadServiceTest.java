package com.rentar.verificacion_service;

import com.rentar.verificacion_service.repository.ITitularidadVehiculoRepository;
import com.rentar.verificacion_service.service.IRegistroTitularidadService.ResultadoTitularidad;
import com.rentar.verificacion_service.service.impl.RegistroTitularidadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RegistroTitularidadServiceTest {

    @Mock
    private ITitularidadVehiculoRepository repository;

    private RegistroTitularidadServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RegistroTitularidadServiceImpl(repository);
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando clienteId coincide con el titular simulado (ej: 89 con DEF456)")
    void testTitularidadCoincidePorClienteId() {
        ResultadoTitularidad res = service.verificarTitularidad(89L, null, "DEF456");
        assertTrue(res.valido());
        assertEquals("30123456", res.documento());
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando documento coincide con el titular simulado")
    void testTitularidadCoincidePorDocumento() {
        ResultadoTitularidad res = service.verificarTitularidad(null, "30123456", "def456");
        assertTrue(res.valido());
    }

    @Test
    @DisplayName("Debe rechazar la titularidad si el cliente no coincide con el dueño de la patente")
    void testTitularidadRechazadaClienteNoCoincide() {
        ResultadoTitularidad res = service.verificarTitularidad(999L, "99999999", "DEF456");
        assertFalse(res.valido());
    }

    @Test
    @DisplayName("Debe rechazar la titularidad si la patente no existe en el registro")
    void testTitularidadRechazadaPatenteInexistente() {
        ResultadoTitularidad res = service.verificarTitularidad(89L, "30123456", "DESCONOCIDA");
        assertFalse(res.valido());
    }
}
