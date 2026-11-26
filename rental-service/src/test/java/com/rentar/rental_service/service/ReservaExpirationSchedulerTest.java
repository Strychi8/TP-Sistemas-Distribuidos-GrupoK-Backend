package com.rentar.rental_service.service;

import com.rentar.rental_service.enums.EstadoReserva;
import com.rentar.rental_service.model.Reserva;
import com.rentar.rental_service.repository.IReservaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservaExpirationSchedulerTest {

    @Test
    void finalizaReservasConfirmadasConFechaFinVencida() {
        IReservaRepository repository = mock(IReservaRepository.class);
        Reserva vencida = Reserva.builder()
                .idReserva(50L)
                .estado(EstadoReserva.CONFIRMADA)
                .build();
        when(repository.findReservasVencidas(eq(EstadoReserva.CONFIRMADA), any(LocalDateTime.class)))
                .thenReturn(List.of(vencida));

        new ReservaExpirationScheduler(repository).finalizarReservasVencidas();

        assertEquals(EstadoReserva.FINALIZADA, vencida.getEstado());
        verify(repository).saveAll(List.of(vencida));
    }

    @Test
    void noGuardaCuandoNoHayReservasVencidas() {
        IReservaRepository repository = mock(IReservaRepository.class);
        when(repository.findReservasVencidas(eq(EstadoReserva.CONFIRMADA), any(LocalDateTime.class)))
                .thenReturn(List.of());

        new ReservaExpirationScheduler(repository).finalizarReservasVencidas();

        verify(repository, org.mockito.Mockito.never()).saveAll(any());
    }
}
