package com.rentar.mantenimiento_service.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoController {

    @GetMapping("/salud")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servicio", "mantenimiento_service",
                "mensaje", "Microservicio de Mantenimiento (Kafka) operativo"
        ));
    }
}
