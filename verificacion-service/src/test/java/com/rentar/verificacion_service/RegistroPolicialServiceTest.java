package com.rentar.verificacion_service;

import com.rentar.verificacion_service.repository.IPatenteRobadaRepository;
import com.rentar.verificacion_service.service.impl.RegistroPolicialServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroPolicialServiceTest {

    @Mock
    private IPatenteRobadaRepository repository;

    private RegistroPolicialServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RegistroPolicialServiceImpl(repository);
    }

    @Test
    @DisplayName("Debe detectar como robada una patente de la lista en memoria (ej: AA111BB)")
    void testPatenteRobadaEnMemoria() {
        assertTrue(service.esRobado("AA111BB"));
        assertTrue(service.esRobado("aa 111 bb")); // con espacios y minusculas
        assertTrue(service.esRobado("ROB123"));
    }

    @Test
    @DisplayName("Debe detectar como limpia una patente que no esta denunciada")
    void testPatenteLimpia() {
        when(repository.existsByPatenteIgnoreCase("OK123OK")).thenReturn(false);
        assertFalse(service.esRobado("OK123OK"));
    }

    @Test
    @DisplayName("Debe detectar como robada una patente si existe en base de datos")
    void testPatenteRobadaEnBaseDeDatos() {
        when(repository.existsByPatenteIgnoreCase("HURTO99")).thenReturn(true);
        assertTrue(service.esRobado("HURTO99"));
    }
}
