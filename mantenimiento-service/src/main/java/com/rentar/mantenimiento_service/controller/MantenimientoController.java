package com.rentar.mantenimiento_service.controller;

import com.rentar.mantenimiento_service.model.SolicitudMantenimiento;
import com.rentar.mantenimiento_service.repository.SolicitudMantenimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Expone consultas para el estado y las solicitudes de mantenimiento. */
@RestController
@RequestMapping("/api/mantenimiento")
@RequiredArgsConstructor
public class MantenimientoController {

    private final SolicitudMantenimientoRepository repository;

    /** Devuelve el estado de disponibilidad del servicio. */
    @GetMapping("/salud")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servicio", "mantenimiento_service",
                "mensaje", "Microservicio de Mantenimiento (Kafka) operativo"
        ));
    }

    /** Lista todas las solicitudes de mantenimiento. */
    @GetMapping("/solicitudes")
    public ResponseEntity<List<SolicitudMantenimiento>> listarSolicitudes() {
        return ResponseEntity.ok(repository.findAll());
    }

    /** Busca la solicitud de mantenimiento asociada a una reserva. */
    @GetMapping("/solicitudes/reserva/{reservaId}")
    public ResponseEntity<SolicitudMantenimiento> obtenerPorReserva(@PathVariable Long reservaId) {
        return repository.findByReservaId(reservaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
