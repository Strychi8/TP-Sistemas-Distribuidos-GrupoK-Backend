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

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroPolicialServiceImpl implements IRegistroPolicialService {

    private final IPatenteRobadaRepository patenteRobadaRepository;

    // Lista en memoria como fallback o cache inmediato
    private final Set<String> patentesRobadasMemoria = ConcurrentHashMap.newKeySet();

    {
        // Patentes simuladas predefinidas
        patentesRobadasMemoria.add("AA111BB");
        patentesRobadasMemoria.add("ZZ999ZZ");
        patentesRobadasMemoria.add("ROB123");
        patentesRobadasMemoria.add("STOLEN1");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esRobado(String patente) {
        if (patente == null || patente.trim().isEmpty()) {
            return false;
        }
        String cleanPatente = normalizarPatente(patente);

        // 1. Verificar en memoria
        if (patentesRobadasMemoria.contains(cleanPatente)) {
            log.warn("[REGISTRO POLICIAL] Patente {} identificada como ROBADA (memoria).", cleanPatente);
            return true;
        }

        // 2. Verificar en base de datos
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

    @Override
    @Transactional
    public PatenteRobada registrarPatenteRobada(String patente, String motivo) {
        String cleanPatente = normalizarPatente(patente);
        patentesRobadasMemoria.add(cleanPatente);

        try {
            PatenteRobada entity = PatenteRobada.builder()
                    .patente(cleanPatente)
                    .motivo(motivo != null ? motivo : "Denuncia por robo simulada")
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

        return patentesRobadasMemoria.stream()
                .map(p -> PatenteRobada.builder().patente(p).motivo("Simulado en memoria").build())
                .toList();
    }

    private String normalizarPatente(String patente) {
        return patente.trim().replaceAll("\\s+", "").toUpperCase();
    }
}
