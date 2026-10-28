package com.rentar.verificacion_service.controller;

import com.rentar.verificacion_service.model.PatenteRobada;
import com.rentar.verificacion_service.model.TitularidadVehiculo;
import com.rentar.verificacion_service.service.IRegistroPolicialService;
import com.rentar.verificacion_service.service.IRegistroTitularidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/verificaciones")
@RequiredArgsConstructor
public class VerificacionController {

    private final IRegistroPolicialService registroPolicialService;
    private final IRegistroTitularidadService registroTitularidadService;

    @GetMapping("/salud")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servicio", "verificacion_service",
                "mensaje", "Microservicio de Verificación Policial y Titularidad (Kafka) operativo"
        ));
    }

    @GetMapping("/patentes-robadas")
    public ResponseEntity<List<PatenteRobada>> listarPatentesRobadas() {
        return ResponseEntity.ok(registroPolicialService.listarPatentesRobadas());
    }

    @PostMapping("/patentes-robadas")
    public ResponseEntity<PatenteRobada> registrarPatenteRobada(@RequestBody Map<String, String> body) {
        String patente = body.get("patente");
        String motivo = body.get("motivo");
        if (patente == null || patente.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroPolicialService.registrarPatenteRobada(patente, motivo));
    }

    @GetMapping("/patentes-robadas/verificar/{patente}")
    public ResponseEntity<Map<String, Object>> verificarPatente(@PathVariable String patente) {
        boolean esRobado = registroPolicialService.esRobado(patente);
        return ResponseEntity.ok(Map.of(
                "patente", patente,
                "esRobado", esRobado,
                "resultado", esRobado ? "ROBADO" : "VALIDADO"
        ));
    }

    @GetMapping("/titularidades")
    public ResponseEntity<List<TitularidadVehiculo>> listarTitularidades() {
        return ResponseEntity.ok(registroTitularidadService.listarTitularidades());
    }

    @PostMapping("/titularidades")
    public ResponseEntity<TitularidadVehiculo> registrarTitularidad(@RequestBody Map<String, Object> body) {
        Long clienteId = body.get("clienteId") != null ? Long.valueOf(body.get("clienteId").toString()) : null;
        String documento = (String) body.get("documento");
        String patente = (String) body.get("patente");
        String titular = (String) body.get("nombreTitular");

        if (patente == null || patente.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroTitularidadService.registrarTitularidad(clienteId, documento, patente, titular));
    }
}
