package com.rentar.mantenimiento_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Expone el endpoint de salud usado para comprobar el servicio. */
@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoSaludController {

    /** Devuelve el estado de disponibilidad del servicio. */
    @GetMapping("/salud")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servicio", "mantenimiento_service",
                "mensaje", "Microservicio de Mantenimiento Preventivo (Kafka) operativo"
        ));
    }
}
