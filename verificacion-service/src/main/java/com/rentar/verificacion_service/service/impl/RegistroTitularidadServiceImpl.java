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

/** Consulta y administra el registro simulado de titularidades vehiculares. */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroTitularidadServiceImpl implements IRegistroTitularidadService {

    private final ITitularidadVehiculoRepository titularidadRepository;

    // Guarda una copia de las titularidades, indexada por patente normalizada.
    private final Map<String, TitularidadVehiculo> titularidadesMemoria = new ConcurrentHashMap<>();

    {
        // Carga relaciones de prueba entre clientes, documentos y patentes.
        agregarTitularidadSimuladaEnMemoria(89L, "30123456", "DEF456", "Ana Martinez");
        agregarTitularidadSimuladaEnMemoria(1L, "40111222", "EXT100", "Juan Perez");
        agregarTitularidadSimuladaEnMemoria(2L, "41222333", "EXT200", "Maria Gomez");
        agregarTitularidadSimuladaEnMemoria(3L, "42333444", "EXT300", "Lucas Fernandez");
        agregarTitularidadSimuladaEnMemoria(4L, "43444555", "EXT400", "Ana Martinez");
    }

    /** Normaliza la patente y agrega la titularidad al registro en memoria. */
    private void agregarTitularidadSimuladaEnMemoria(
            Long clienteId,
            String documento,
            String patente,
            String titular) {
        String cleanPatente = normalizarPatente(patente);
        titularidadesMemoria.put(cleanPatente, TitularidadVehiculo.builder()
                .clienteId(clienteId)
                .documento(documento)
                .patente(cleanPatente)
                .nombreTitular(titular)
                .build());
    }

    /** Compara los datos recibidos con el registro de la patente. */
    @Override
    @Transactional(readOnly = true)
    public ResultadoTitularidad verificarTitularidad(Long clienteId, String documento, String patente) {
        if (patente == null || patente.trim().isEmpty()) {
            return new ResultadoTitularidad(false, documento, null);
        }

        String cleanPatente = normalizarPatente(patente);
        log.info("[REGISTRO TITULARIDAD] Verificando titularidad para patente={}, clienteId={}, documento={}",
                cleanPatente, clienteId, documento);

        // Prioriza la base de datos cuando contiene un registro para la patente.
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

        // Usa la copia en memoria si no hay registro o falla la consulta a la base.
        TitularidadVehiculo titularidadMem = titularidadesMemoria.get(cleanPatente);
        if (titularidadMem != null) {
            boolean coincide = coincideTitular(titularidadMem, clienteId, documento);
            log.info("[REGISTRO TITULARIDAD] Encontrado en memoria. Coincide: {}", coincide);
            return new ResultadoTitularidad(coincide, titularidadMem.getDocumento(), titularidadMem.getNombreTitular());
        }

        log.warn("[REGISTRO TITULARIDAD] No existe registro de titularidad para la patente {}", cleanPatente);
        return new ResultadoTitularidad(false, documento, null);
    }

    /** Acepta la coincidencia del identificador del cliente o del documento. */
    private boolean coincideTitular(TitularidadVehiculo reg, Long clienteId, String documento) {
        // El registro es válido si coincide cualquiera de los dos identificadores.
        if (clienteId != null && reg.getClienteId() != null && clienteId.equals(reg.getClienteId())) {
            return true;
        }
        if (documento != null && !documento.trim().isEmpty() &&
                reg.getDocumento() != null && reg.getDocumento().trim().equalsIgnoreCase(documento.trim())) {
            return true;
        }
        return false;
    }

    /** Actualiza la copia en memoria y persiste la titularidad cuando es posible. */
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

    /** Devuelve los registros persistidos o la copia en memoria si la base no tiene registros. */
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

    /** Quita espacios y convierte la patente a mayúsculas. */
    private String normalizarPatente(String patente) {
        return patente.trim().replaceAll("\\s+", "").toUpperCase();
    }
}
