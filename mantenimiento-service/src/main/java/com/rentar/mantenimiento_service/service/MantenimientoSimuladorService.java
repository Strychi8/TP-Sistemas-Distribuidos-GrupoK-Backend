package com.rentar.mantenimiento_service.service;

import com.rentar.mantenimiento_service.model.EstadoMantenimiento;
import com.rentar.mantenimiento_service.model.SolicitudMantenimiento;
import com.rentar.mantenimiento_service.repository.SolicitudMantenimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MantenimientoSimuladorService {

    private final MantenimientoService service;
    private final SolicitudMantenimientoRepository repository;

    /**
     * FASE 2: Simulación no bloqueante fuera de la transacción.
     */
    @Async
    public void simularYFinalizarMantenimiento(Long solicitudId) {
        simularYFinalizarMantenimiento(solicitudId, null);
    }

    @Async
    public void simularYFinalizarMantenimiento(Long solicitudId, Long vehiculoIdParam) {
        if (solicitudId == null) {
            log.error("[SIMULADOR] solicitudId nulo recibido en simulador.");
            return;
        }

        log.info("[SIMULADOR] Iniciando proceso de mantenimiento simulado para solicitud ID={}, vehiculoId={}",
                solicitudId, vehiculoIdParam);
        try {
            // Simular el proceso (2 a 3 segundos de trabajo fuera de la BD)
            Thread.sleep(2500);

            Long vehiculoId = vehiculoIdParam;
            if (vehiculoId == null) {
                // Consultar la solicitud para conocer el vehículo involucrado si no vino por parámetro
                Optional<SolicitudMantenimiento> solicitudOpt = repository.findById(solicitudId);
                vehiculoId = solicitudOpt.map(SolicitudMantenimiento::getVehiculoId).orElse(null);
            }

            // REGLA DE SIMULACIÓN PARA PRUEBAS:
            // El vehículo ID=3 (Volkswagen Gol Trend) falla la inspección preventiva (NO_REALIZADO)
            // simulando desperfectos mecánicos o no superación de checklist técnico.
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

            // FASE 3: Guardar el resultado final en una NUEVA transacción
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
