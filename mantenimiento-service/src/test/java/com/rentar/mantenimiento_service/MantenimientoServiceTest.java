package com.rentar.mantenimiento_service;

import com.rentar.mantenimiento_service.kafka.event.MantenimientoCompletadoInternalEvent;
import com.rentar.mantenimiento_service.kafka.event.ReservaConfirmadaEvent;
import com.rentar.mantenimiento_service.model.EstadoMantenimiento;
import com.rentar.mantenimiento_service.model.SolicitudMantenimiento;
import com.rentar.mantenimiento_service.repository.SolicitudMantenimientoRepository;
import com.rentar.mantenimiento_service.service.MantenimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MantenimientoServiceTest {

    @Mock
    private SolicitudMantenimientoRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MantenimientoService service;

    private ReservaConfirmadaEvent eventoReserva;

    @BeforeEach
    void setUp() {
        eventoReserva = new ReservaConfirmadaEvent(
                150L,
                15L,
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 5, 10, 0)
        );
    }

    @Test
    @DisplayName("crearSolicitudMantenimiento guarda en repositorio y retorna el ID autogenerado")
    void crearSolicitudMantenimiento_Exito() {
        SolicitudMantenimiento guardada = SolicitudMantenimiento.builder()
                .id(1L)
                .reservaId(150L)
                .vehiculoId(15L)
                .estado(EstadoMantenimiento.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(repository.save(any(SolicitudMantenimiento.class))).thenReturn(guardada);

        Long idGenerado = service.crearSolicitudMantenimiento(eventoReserva);

        assertThat(idGenerado).isEqualTo(1L);

        ArgumentCaptor<SolicitudMantenimiento> captor = ArgumentCaptor.forClass(SolicitudMantenimiento.class);
        verify(repository).save(captor.capture());
        SolicitudMantenimiento enviada = captor.getValue();

        assertThat(enviada.getReservaId()).isEqualTo(150L);
        assertThat(enviada.getVehiculoId()).isEqualTo(15L);
        assertThat(enviada.getEstado()).isEqualTo(EstadoMantenimiento.PENDIENTE);
    }

    @Test
    @DisplayName("completarMantenimiento actualiza el estado a REALIZADO y publica evento interno")
    void completarMantenimiento_Exito() {
        SolicitudMantenimiento existente = SolicitudMantenimiento.builder()
                .id(1L)
                .reservaId(150L)
                .vehiculoId(15L)
                .estado(EstadoMantenimiento.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(SolicitudMantenimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        service.completarMantenimiento(1L, EstadoMantenimiento.REALIZADO);

        assertThat(existente.getEstado()).isEqualTo(EstadoMantenimiento.REALIZADO);
        verify(repository).save(existente);

        ArgumentCaptor<MantenimientoCompletadoInternalEvent> eventCaptor =
                ArgumentCaptor.forClass(MantenimientoCompletadoInternalEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        MantenimientoCompletadoInternalEvent eventoPublicado = eventCaptor.getValue();
        assertThat(eventoPublicado.getReservaId()).isEqualTo(150L);
        assertThat(eventoPublicado.getVehiculoId()).isEqualTo(15L);
        assertThat(eventoPublicado.getResultado()).isEqualTo("REALIZADO");
    }

    @Test
    @DisplayName("completarMantenimiento lanza IllegalArgumentException si la solicitud no existe")
    void completarMantenimiento_NoExiste_LanzaExcepcion() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completarMantenimiento(999L, EstadoMantenimiento.REALIZADO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontró la solicitud con ID: 999");

        verify(eventPublisher, never()).publishEvent(any());
    }
}
