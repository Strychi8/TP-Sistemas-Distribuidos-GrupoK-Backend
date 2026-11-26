package com.rentar.verificacion_service.service.impl;

import com.rentar.verificacion_service.model.PatenteRobada;
import com.rentar.verificacion_service.repository.IPatenteRobadaRepository;
import com.rentar.verificacion_service.service.IRegistroPolicialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Consulta y administra el registro policial simulado de patentes denunciadas. */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroPolicialServiceImpl implements IRegistroPolicialService {

    private final IPatenteRobadaRepository patenteRobadaRepository;

    // Conserva una copia en memoria para consultar patentes si falla la base de datos.
    private final Set<String> patentesRobadasEnMemoria = ConcurrentHashMap.newKeySet();

    {
        // Carga patentes de prueba que simulan denuncias o pedidos de secuestro.
        patentesRobadasEnMemoria.add("AA111BB");
        patentesRobadasEnMemoria.add("ZZ999ZZ");
        patentesRobadasEnMemoria.add("ROB123");
        patentesRobadasEnMemoria.add("STO123");
        patentesRobadasEnMemoria.add("EXT200"); // Patente de prueba para validar el rechazo.
    }

    /** Comprueba primero la lista en memoria y luego el registro de la base de datos. */
    @Override
    @Transactional(readOnly = true)
    public boolean esRobado(String patente) {
        if (patente == null || patente.trim().isEmpty()) {
            return false;
        }
        String cleanPatente = normalizarPatente(patente);

        // Revisa primero la copia en memoria.
        if (patentesRobadasEnMemoria.contains(cleanPatente)) {
            log.warn("[REGISTRO POLICIAL] Patente {} identificada como ROBADA (memoria).", cleanPatente);
            return true;
        }

        // Consulta la base de datos si no encontró la patente en memoria.
        try {
            boolean existeEnDb = patenteRobadaRepository.existsByPatenteIgnoreCase(cleanPatente);
            if (existeEnDb) {
                log.warn("[REGISTRO POLICIAL] Patente {} identificada como ROBADA (BD).", cleanPatente);
                return true;
            }
        } catch (Exception e) {
            log.error("[REGISTRO POLICIAL] Error consultando BD para patente {}. Usando fallback memoria: {}",
                    cleanPatente, e.getMessage());
        }

        log.info("[REGISTRO POLICIAL] Patente {} consultada y se encuentra LIMPIA.", cleanPatente);
        return false;
    }

    /** Registra la patente en memoria y en la base de datos cuando está disponible. */
    @Override
    @Transactional
    public PatenteRobada registrarPatenteRobada(String patente, String motivo) {
        String cleanPatente = normalizarPatente(patente);
        patentesRobadasEnMemoria.add(cleanPatente);

        try {
            PatenteRobada entity = PatenteRobada.builder()
                    .patente(cleanPatente)
                    .motivo(motivo != null ? motivo : "Denuncia por robo")
                    .build();
            return patenteRobadaRepository.save(entity);
        } catch (Exception e) {
            log.error("[REGISTRO POLICIAL] Error persistiendo patente robada {}: {}", cleanPatente, e.getMessage());
            return PatenteRobada.builder()
                    .patente(cleanPatente)
                    .motivo(motivo)
                    .build();
        }
    }

    /** Lista los registros de la base de datos o usa la copia en memoria como respaldo. */
    @Override
    @Transactional(readOnly = true)
    public List<PatenteRobada> listarPatentesRobadas() {
        try {
            List<PatenteRobada> list = patenteRobadaRepository.findAll();
            if (!list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            log.error("[REGISTRO POLICIAL] Error listando patentes de BD: {}", e.getMessage());
        }

        return patentesRobadasEnMemoria.stream()
                .map(p -> PatenteRobada.builder().patente(p).motivo("Simulado en memoria").build())
                .toList();
    }

    /** Quita espacios y convierte la patente a mayúsculas. */
    private String normalizarPatente(String patente) {
        return patente.trim().replaceAll("\\s+", "").toUpperCase();
    }
}
