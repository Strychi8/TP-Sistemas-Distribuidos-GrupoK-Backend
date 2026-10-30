package com.rentar.rental_service.kafka.consumer;

import com.rentar.rental_service.kafka.event.MantenimientoResultadoEvent;
import com.rentar.rental_service.model.Reserva;
import com.rentar.rental_service.repository.IReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MantenimientoResultadoConsumerTest {

    @Mock
    private IReservaRepository reservaRepository;

    private MantenimientoResultadoConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new MantenimientoResultadoConsumer(reservaRepository);
    }

    @Test
    void debeActualizarResultadoCuandoElEventoEsValido() {
        Reserva reserva = Reserva.builder()
                .idReserva(1L)
                .resultadoMantenimiento(null)
                .build();

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        consumer.consumirResultado(
                new MantenimientoResultadoEvent(1L, 10L, "REALIZADO")
        );

        assertEquals("REALIZADO", reserva.getResultadoMantenimiento());
        assertNotNull(reserva.getFechaResultadoMantenimiento());

        verify(reservaRepository).save(reserva);
    }

    @Test
    void noDebeGuardarCuandoElEventoEsNulo() {
        consumer.consumirResultado(null);

        verifyNoInteractions(reservaRepository);
    }

    @Test
    void noDebeGuardarCuandoElResultadoEsInvalido() {
        consumer.consumirResultado(
                new MantenimientoResultadoEvent(1L, 10L, "PENDIENTE")
        );

        verifyNoInteractions(reservaRepository);
    }

    @Test
    void noDebeGuardarCuandoLaReservaNoExiste() {
        when(reservaRepository.findById(1L))
                .thenReturn(Optional.empty());

        consumer.consumirResultado(
                new MantenimientoResultadoEvent(1L, 10L, "REALIZADO")
        );

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void noDebeGuardarNuevamenteSiElResultadoYaCoincide() {
        Reserva reserva = Reserva.builder()
                .idReserva(1L)
                .resultadoMantenimiento("REALIZADO")
                .build();

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        consumer.consumirResultado(
                new MantenimientoResultadoEvent(1L, 10L, "REALIZADO")
        );

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void debeActualizarCuandoElResultadoCambia() {
        Reserva reserva = Reserva.builder()
                .idReserva(1L)
                .resultadoMantenimiento("NO_REALIZADO")
                .build();

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        consumer.consumirResultado(
                new MantenimientoResultadoEvent(1L, 10L, "REALIZADO")
        );

        assertEquals("REALIZADO", reserva.getResultadoMantenimiento());
        assertNotNull(reserva.getFechaResultadoMantenimiento());

        verify(reservaRepository).save(reserva);
    }
}
