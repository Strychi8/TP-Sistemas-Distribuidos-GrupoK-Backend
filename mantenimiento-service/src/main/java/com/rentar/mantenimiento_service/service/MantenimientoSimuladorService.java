package com.rentar.mantenimiento_service.service;

import com.rentar.mantenimiento_service.model.EstadoMantenimiento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Simula el trabajo asincrónico del mantenimiento preventivo.
 *
 * <p>Permite que la confirmación de una reserva continúe sin esperar el
 * resultado del mantenimiento y luego delega su cierre al servicio
 * transaccional.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MantenimientoSimuladorService {

    private final MantenimientoService service;

    /**
     * FASE 2: Simulación no bloqueante fuera de la transacción.
     */
    @Async
    public void simularYFinalizarMantenimiento(Long solicitudId) {
        if (solicitudId == null) {
            log.error("[SIMULADOR] solicitudId nulo recibido en simulador.");
            return;
        }

        log.info("[SIMULADOR] Iniciando proceso de mantenimiento simulado para solicitud ID={}", solicitudId);
        try {
            // Simular el proceso (2 a 3 segundos de trabajo fuera de la BD)
            Thread.sleep(2500);

            // Resultado del mantenimiento preventivo (REALIZADO de acuerdo al enunciado)
            EstadoMantenimiento resultado = EstadoMantenimiento.REALIZADO;
            log.info("[SIMULADOR] Proceso preventivo finalizado con exito para solicitud ID={}. Estado={}",
                    solicitudId, resultado);

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
