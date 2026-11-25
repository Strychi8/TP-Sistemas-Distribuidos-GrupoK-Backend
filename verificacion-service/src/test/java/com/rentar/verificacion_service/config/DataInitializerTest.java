package com.rentar.verificacion_service.config;

import com.rentar.verificacion_service.model.PatenteRobada;
import com.rentar.verificacion_service.repository.IPatenteRobadaRepository;
import com.rentar.verificacion_service.repository.ITitularidadVehiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private IPatenteRobadaRepository patenteRobadaRepository;

    @Mock
    private ITitularidadVehiculoRepository titularidadRepository;

    @Test
    void migraLaPatenteRobadaInvalidaCuandoNoExisteLaNueva() {
        PatenteRobada patenteAntigua = PatenteRobada.builder()
                .patente("STOLEN1")
                .build();
        when(patenteRobadaRepository.count()).thenReturn(1L);
        when(patenteRobadaRepository.findByPatenteIgnoreCase("STOLEN1"))
                .thenReturn(Optional.of(patenteAntigua));
        when(patenteRobadaRepository.existsByPatenteIgnoreCase("STO123"))
                .thenReturn(false);
        when(titularidadRepository.count()).thenReturn(1L);

        new DataInitializer(patenteRobadaRepository, titularidadRepository).run();

        assertEquals("STO123", patenteAntigua.getPatente());
        verify(patenteRobadaRepository).save(patenteAntigua);
    }

    @Test
    void eliminaLaPatenteInvalidaSiLaNuevaYaExiste() {
        PatenteRobada patenteAntigua = PatenteRobada.builder()
                .patente("STOLEN1")
                .build();
        when(patenteRobadaRepository.count()).thenReturn(1L);
        when(patenteRobadaRepository.findByPatenteIgnoreCase("STOLEN1"))
                .thenReturn(Optional.of(patenteAntigua));
        when(patenteRobadaRepository.existsByPatenteIgnoreCase("STO123"))
                .thenReturn(true);
        when(titularidadRepository.count()).thenReturn(1L);

        new DataInitializer(patenteRobadaRepository, titularidadRepository).run();

        verify(patenteRobadaRepository).delete(patenteAntigua);
    }
}
