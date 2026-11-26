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

/** Registra las solicitudes de mantenimiento y guarda sus resultados. */
@Service
@RequiredArgsConstructor
@Slf4j
public class MantenimientoService {

    private final SolicitudMantenimientoRepository repository;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Crea la solicitud en una transacción corta.
     * La restricción única de reservaId impide registrar duplicados.
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
     * Guarda el resultado y publica un evento interno dentro de la transacción.
     * El productor Kafka procesa ese evento después del commit.
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

        // Publica un evento interno de Spring; el listener lo enviará a Kafka tras el commit.
        eventPublisher.publishEvent(new MantenimientoCompletadoInternalEvent(
                solicitud.getReservaId(),
                solicitud.getVehiculoId(),
                resultado.name()
        ));
    }

}
