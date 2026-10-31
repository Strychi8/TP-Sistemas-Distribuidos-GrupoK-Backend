package com.rentar.mantenimiento_service.service;

import com.rentar.mantenimiento_service.kafka.event.MantenimientoCompletadoInternalEvent;
import com.rentar.mantenimiento_service.kafka.event.ReservaConfirmadaEvent;
import com.rentar.mantenimiento_service.model.EstadoMantenimiento;
import com.rentar.mantenimiento_service.model.SolicitudMantenimiento;
import com.rentar.mantenimiento_service.repository.SolicitudMantenimientoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MantenimientoService {

    private final SolicitudMantenimientoRepository repository;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * FASE 1: Registro Inicial
     * Se ejecuta dentro de una transacción corta. Si reservaId ya existe,
     * MySQL lanzará DataIntegrityViolationException y la transacción se revertirá.
     */
    @Transactional
    public Long crearSolicitudMantenimiento(ReservaConfirmadaEvent event) {
        log.info("[MANTENIMIENTO] Registrando solicitud preventiva: reservaId={}, vehiculoId={}",
                event.getReservaId(), event.getVehiculoId());

        SolicitudMantenimiento solicitud = SolicitudMantenimiento.builder()
                .reservaId(event.getReservaId())
                .vehiculoId(event.getVehiculoId())
                .fechaInicio(event.getFechaInicio())
                .fechaFin(event.getFechaFin())
                .fechaCreacion(LocalDateTime.now())
                .build();

        solicitud = repository.save(solicitud);
        log.info("[MANTENIMIENTO] Solicitud creada con ID={}, estado={}", solicitud.getId(), solicitud.getEstado());
        return solicitud.getId();
    }

    /**
     * FASE 3: Transacción de Cierre y Emisión del Evento Local.
     */
    @Transactional
    public void completarMantenimiento(Long solicitudId, EstadoMantenimiento resultado) {
        log.info("[MANTENIMIENTO] Completando solicitud ID={} con resultado={}", solicitudId, resultado);

        SolicitudMantenimiento solicitud = repository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la solicitud con ID: " + solicitudId));

        solicitud.setEstado(resultado);
        repository.save(solicitud);

        log.info("[MANTENIMIENTO] Solicitud ID={} actualizada a {}. Emitiendo evento interno para Kafka.",
                solicitudId, resultado);

        // Emitimos el evento interno en la memoria de Spring (no a kafka directamente)
        eventPublisher.publishEvent(new MantenimientoCompletadoInternalEvent(
                solicitud.getReservaId(),
                solicitud.getVehiculoId(),
                resultado.name() // Se pasa como String "REALIZADO"
        ));
    }

}
