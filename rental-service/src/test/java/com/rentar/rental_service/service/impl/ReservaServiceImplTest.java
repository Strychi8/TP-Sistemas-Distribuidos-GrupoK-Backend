package com.rentar.rental_service.service.impl;

import com.rentar.rental_service.dto.request.ReservaRequestDTO;
import com.rentar.rental_service.dto.response.ReservaResponseDTO;
import com.rentar.rental_service.enums.EstadoReserva;
import com.rentar.rental_service.kafka.event.ReservaConfirmadaEvent;
import com.rentar.rental_service.mapper.ReservaMapper;
import com.rentar.rental_service.model.Reserva;
import com.rentar.rental_service.repository.IReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservaServiceImplTest {

    private IReservaRepository reservaRepository;
    private ReservaMapper reservaMapper;
    private ApplicationEventPublisher eventPublisher;
    private ReservaServiceImpl reservaService;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(IReservaRepository.class);
        reservaMapper = new ReservaMapper();
        eventPublisher = mock(ApplicationEventPublisher.class);

        reservaService = new ReservaServiceImpl(
                reservaRepository,
                reservaMapper,
                eventPublisher
        );
    }

    @Test
    void crearReservaDebePublicarEventoYDevolverRespuesta() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(2);
        LocalDateTime fin = inicio.plusDays(3);

        ReservaRequestDTO dto = new ReservaRequestDTO(
                5L,
                10L,
                inicio,
                fin,
                new BigDecimal("100.00")
        );

        when(reservaRepository.existeSolapamiento(
                10L, inicio, fin, EstadoReserva.CONFIRMADA
        )).thenReturn(false);

        when(reservaRepository.save(any(Reserva.class)))
                .thenAnswer(invocation -> {
                    Reserva reserva = invocation.getArgument(0);
                    reserva.setIdReserva(1L);
                    return reserva;
                });

        ReservaResponseDTO respuesta = reservaService.crearReserva(dto);

        assertNotNull(respuesta);
        assertEquals(1L, respuesta.idReserva());
        assertEquals(EstadoReserva.CONFIRMADA, respuesta.estadoReserva());

        verify(eventPublisher).publishEvent(
                any(ReservaConfirmadaEvent.class)
        );
    }

    @Test
    void cancelarReservaNoDebePublicarEventosNiModificarDatosDeMantenimiento() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(5);
        LocalDateTime fin = inicio.plusDays(7);
        LocalDateTime fechaResultado =
                LocalDateTime.now().minusDays(1);

        Reserva reserva = Reserva.builder()
                .idReserva(1L)
                .idCliente(5L)
                .idVehiculo(10L)
                .fechaInicio(inicio)
                .fechaFin(fin)
                .precioDiario(new BigDecimal("100.00"))
                .importeTotal(new BigDecimal("700.00"))
                .estado(EstadoReserva.CONFIRMADA)
                .resultadoMantenimiento("REALIZADO")
                .fechaResultadoMantenimiento(fechaResultado)
                .build();

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        when(reservaRepository.save(any(Reserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservaResponseDTO respuesta =
                reservaService.cancelarReserva(1L);

        assertEquals(EstadoReserva.CANCELADA, respuesta.estadoReserva());
        assertEquals("REALIZADO", reserva.getResultadoMantenimiento());
        assertEquals(
                fechaResultado,
                reserva.getFechaResultadoMantenimiento()
        );

        verify(reservaRepository).save(reserva);
        verifyNoInteractions(eventPublisher);
    }
}
