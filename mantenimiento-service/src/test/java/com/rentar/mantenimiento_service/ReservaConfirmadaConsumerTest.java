package com.rentar.mantenimiento_service;

import com.rentar.mantenimiento_service.kafka.consumer.ReservaConfirmadaConsumer;
import com.rentar.mantenimiento_service.kafka.event.ReservaConfirmadaEvent;
import com.rentar.mantenimiento_service.service.MantenimientoService;
import com.rentar.mantenimiento_service.service.MantenimientoSimuladorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaConfirmadaConsumerTest {

    @Mock
    private MantenimientoService mantenimientoService;

    @Mock
    private MantenimientoSimuladorService simuladorService;

    @InjectMocks
    private ReservaConfirmadaConsumer consumer;

    @Test
    @DisplayName("consumirReservaConfirmada delega correctamente la creación y simulación de mantenimiento")
    void consumirReservaConfirmada_Exito() {
        ReservaConfirmadaEvent event = new ReservaConfirmadaEvent(
                150L,
                15L,
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 5, 10, 0)
        );

        when(mantenimientoService.crearSolicitudMantenimiento(event)).thenReturn(10L);

        consumer.consumirReservaConfirmada(event);

        verify(mantenimientoService).crearSolicitudMantenimiento(event);
        verify(simuladorService).simularYFinalizarMantenimiento(10L);
    }

    @Test
    @DisplayName("consumirReservaConfirmada maneja DataIntegrityViolationException por reserva duplicada (idempotencia)")
    void consumirReservaConfirmada_Duplicado_Idempotente() {
        ReservaConfirmadaEvent event = new ReservaConfirmadaEvent(
                150L,
                15L,
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 5, 10, 0)
        );

        when(mantenimientoService.crearSolicitudMantenimiento(event))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry '150' for key 'reserva_id'"));

        consumer.consumirReservaConfirmada(event);

        verify(mantenimientoService).crearSolicitudMantenimiento(event);
        verify(simuladorService, never()).simularYFinalizarMantenimiento(any());
    }

    @Test
    @DisplayName("consumirReservaConfirmada con evento nulo no ejecuta procesamiento")
    void consumirReservaConfirmada_EventoNulo_Ignora() {
        consumer.consumirReservaConfirmada(null);

        verify(mantenimientoService, never()).crearSolicitudMantenimiento(any());
        verify(simuladorService, never()).simularYFinalizarMantenimiento(any());
    }
}
