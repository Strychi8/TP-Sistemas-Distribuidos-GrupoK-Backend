package com.rentar.verificacion_service.service.impl;

import com.rentar.verificacion_service.model.TitularidadVehiculo;
import com.rentar.verificacion_service.repository.ITitularidadVehiculoRepository;
import com.rentar.verificacion_service.service.IRegistroTitularidadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroTitularidadServiceImpl implements IRegistroTitularidadService {

    private final ITitularidadVehiculoRepository titularidadRepository;

    // Cache / Fallback en memoria de titularidades simuladas (clave: patente normalizada)
    private final Map<String, TitularidadVehiculo> titularidadesMemoria = new ConcurrentHashMap<>();

    {
        // Datos simulados iniciales (incluyendo el caso del enunciado oficial Hito 3)
        // Ejemplo del enunciado: clienteId=89, documento=30123456, patente=DEF456
        agregarMemoria(89L, "30123456", "DEF456", "Ana Martinez");
        agregarMemoria(1L, "11111111", "EXT100", "Juan Perez");
        agregarMemoria(2L, "22222222", "EXT200", "Maria Gomez");
    }

    private void agregarMemoria(Long clienteId, String documento, String patente, String titular) {
        String cleanPatente = normalizarPatente(patente);
        titularidadesMemoria.put(cleanPatente, TitularidadVehiculo.builder()
                .clienteId(clienteId)
                .documento(documento)
                .patente(cleanPatente)
                .nombreTitular(titular)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoTitularidad verificarTitularidad(Long clienteId, String documento, String patente) {
        if (patente == null || patente.trim().isEmpty()) {
            return new ResultadoTitularidad(false, documento, null);
        }

        String cleanPatente = normalizarPatente(patente);
        log.info("[REGISTRO TITULARIDAD] Verificando titularidad para patente={}, clienteId={}, documento={}",
                cleanPatente, clienteId, documento);

        // 1. Consultar en base de datos si está disponible
        try {
            Optional<TitularidadVehiculo> titularidadDb = titularidadRepository.findByPatenteIgnoreCase(cleanPatente);
            if (titularidadDb.isPresent()) {
                TitularidadVehiculo t = titularidadDb.get();
                boolean coincide = coincideTitular(t, clienteId, documento);
                log.info("[REGISTRO TITULARIDAD] Encontrado en BD. Coincide: {}", coincide);
                return new ResultadoTitularidad(coincide, t.getDocumento(), t.getNombreTitular());
            }
        } catch (Exception e) {
            log.error("[REGISTRO TITULARIDAD] Error consultando BD. Usando memoria: {}", e.getMessage());
        }

        // 2. Consultar en fallback de memoria
        TitularidadVehiculo titularidadMem = titularidadesMemoria.get(cleanPatente);
        if (titularidadMem != null) {
            boolean coincide = coincideTitular(titularidadMem, clienteId, documento);
            log.info("[REGISTRO TITULARIDAD] Encontrado en memoria. Coincide: {}", coincide);
            return new ResultadoTitularidad(coincide, titularidadMem.getDocumento(), titularidadMem.getNombreTitular());
        }

        log.warn("[REGISTRO TITULARIDAD] No existe registro de titularidad para la patente {}", cleanPatente);
        return new ResultadoTitularidad(false, documento, null);
    }

    private boolean coincideTitular(TitularidadVehiculo reg, Long clienteId, String documento) {
        // Coincide si coincide el clienteId O el documento
        if (clienteId != null && reg.getClienteId() != null && clienteId.equals(reg.getClienteId())) {
            return true;
        }
        if (documento != null && !documento.trim().isEmpty() &&
                reg.getDocumento() != null && reg.getDocumento().trim().equalsIgnoreCase(documento.trim())) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public TitularidadVehiculo registrarTitularidad(Long clienteId, String documento, String patente, String titular) {
        String cleanPatente = normalizarPatente(patente);
        TitularidadVehiculo entity = TitularidadVehiculo.builder()
                .clienteId(clienteId)
                .documento(documento)
                .patente(cleanPatente)
                .nombreTitular(titular != null ? titular : "Titular Registrado")
                .build();

        titularidadesMemoria.put(cleanPatente, entity);

        try {
            return titularidadRepository.save(entity);
        } catch (Exception e) {
            log.error("[REGISTRO TITULARIDAD] Error persistiendo titularidad: {}", e.getMessage());
            return entity;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TitularidadVehiculo> listarTitularidades() {
        try {
            List<TitularidadVehiculo> list = titularidadRepository.findAll();
            if (!list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            log.error("[REGISTRO TITULARIDAD] Error listando titularidades de BD: {}", e.getMessage());
        }
        return List.copyOf(titularidadesMemoria.values());
    }

    private String normalizarPatente(String patente) {
        return patente.trim().replaceAll("\\s+", "").toUpperCase();
    }
}
