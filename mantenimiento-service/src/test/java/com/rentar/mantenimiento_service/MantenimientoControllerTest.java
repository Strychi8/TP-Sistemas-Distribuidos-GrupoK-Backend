package com.rentar.mantenimiento_service;

import com.rentar.mantenimiento_service.controller.MantenimientoController;
import com.rentar.mantenimiento_service.model.EstadoMantenimiento;
import com.rentar.mantenimiento_service.model.SolicitudMantenimiento;
import com.rentar.mantenimiento_service.repository.SolicitudMantenimientoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MantenimientoControllerTest {

    @Mock
    private SolicitudMantenimientoRepository repository;

    @InjectMocks
    private MantenimientoController controller;

    @Test
    @DisplayName("GET /api/mantenimiento/salud responde status UP")
    void health_RespondeUP() {
        ResponseEntity<Map<String, String>> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("servicio")).isEqualTo("mantenimiento_service");
    }

    @Test
    @DisplayName("GET /api/mantenimiento/solicitudes devuelve lista de solicitudes")
    void listarSolicitudes_DevuelveLista() {
        SolicitudMantenimiento sol = SolicitudMantenimiento.builder()
                .id(1L)
                .reservaId(150L)
                .vehiculoId(15L)
                .estado(EstadoMantenimiento.REALIZADO)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(repository.findAll()).thenReturn(List.of(sol));

        ResponseEntity<List<SolicitudMantenimiento>> response = controller.listarSolicitudes();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getReservaId()).isEqualTo(150L);
    }

    @Test
    @DisplayName("GET /api/mantenimiento/solicitudes/reserva/{reservaId} devuelve solicitud existente")
    void obtenerPorReserva_Existente() {
        SolicitudMantenimiento sol = SolicitudMantenimiento.builder()
                .id(1L)
                .reservaId(150L)
                .vehiculoId(15L)
                .estado(EstadoMantenimiento.REALIZADO)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(repository.findByReservaId(150L)).thenReturn(Optional.of(sol));

        ResponseEntity<SolicitudMantenimiento> response = controller.obtenerPorReserva(150L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getReservaId()).isEqualTo(150L);
    }

    @Test
    @DisplayName("GET /api/mantenimiento/solicitudes/reserva/{reservaId} devuelve 404 si no existe")
    void obtenerPorReserva_NoExiste_404() {
        when(repository.findByReservaId(999L)).thenReturn(Optional.empty());

        ResponseEntity<SolicitudMantenimiento> response = controller.obtenerPorReserva(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
