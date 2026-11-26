package com.rentar.mantenimiento_service.service;

import com.rentar.mantenimiento_service.model.EstadoMantenimiento;
import com.rentar.mantenimiento_service.model.SolicitudMantenimiento;
import com.rentar.mantenimiento_service.repository.SolicitudMantenimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Ejecuta la simulación preventiva fuera del flujo que recibe la reserva. */
@Service
@RequiredArgsConstructor
@Slf4j
public class MantenimientoSimuladorService {

    private final MantenimientoService service;
    private final SolicitudMantenimientoRepository repository;

    /** Simula el mantenimiento y obtiene el vehículo desde la solicitud guardada. */
    @Async
    public void simularYFinalizarMantenimiento(Long solicitudId) {
        simularYFinalizarMantenimiento(solicitudId, null);
    }

    /** Simula el mantenimiento y usa el ID de vehículo recibido si está disponible. */
    @Async
    public void simularYFinalizarMantenimiento(Long solicitudId, Long vehiculoIdParam) {
        if (solicitudId == null) {
            log.error("[SIMULADOR] solicitudId nulo recibido en simulador.");
            return;
        }

        log.info("[SIMULADOR] Iniciando proceso de mantenimiento simulado para solicitud ID={}, vehiculoId={}",
                solicitudId, vehiculoIdParam);
        try {
            // Simula una inspección de 2,5 segundos sin mantener abierta una transacción.
            Thread.sleep(2500);

            Long vehiculoId = vehiculoIdParam;
            if (vehiculoId == null) {
                // Obtiene el vehículo desde la solicitud si no recibió su ID.
                Optional<SolicitudMantenimiento> solicitudOpt = repository.findById(solicitudId);
                vehiculoId = solicitudOpt.map(SolicitudMantenimiento::getVehiculoId).orElse(null);
            }

            // Regla de prueba: el vehículo con ID 3 obtiene el resultado NO_REALIZADO.
            EstadoMantenimiento resultado;
            if (vehiculoId != null && vehiculoId == 3L) {
                resultado = EstadoMantenimiento.NO_REALIZADO;
                log.warn("[SIMULADOR] Inspección preventiva rechazada/no completada para solicitud ID={}, vehiculoId=3 (Gol Trend). Estado={}",
                        solicitudId, resultado);
            } else {
                resultado = EstadoMantenimiento.REALIZADO;
                log.info("[SIMULADOR] Proceso preventivo finalizado con exito para solicitud ID={}, vehiculoId={}. Estado={}",
                        solicitudId, vehiculoId, resultado);
            }

            // Guarda el resultado en una nueva transacción.
            service.completarMantenimiento(solicitudId, resultado);

        } catch (InterruptedException e) {
            log.warn("[SIMULADOR] Proceso de simulacion interrumpido para solicitud ID={}", solicitudId);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[SIMULADOR] Error durante simulacion para solicitud ID={}: {}",
                    solicitudId, e.getMessage(), e);
        }
    }

}
