package com.rentar.mantenimiento_service.service;

import com.rentar.mantenimiento_service.model.EstadoMantenimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MantenimientoSimuladorService {

    private final MantenimientoService service;

    /**
     * FASE 2: Simulación no bloqueante fuera de la transacción.
     */
    @Async
    public void simularYFinalizarMantenimiento(Long solicitudId) {
        try {
            // Simular el proceso (2 a 3 segundos de trabajo fuera de la BD)
            Thread.sleep(2500);

            // Resultado simplificado como REALIZADO (o aleatorio)
            EstadoMantenimiento resultado = EstadoMantenimiento.REALIZADO;

            // FASE 3: Guardar el resultado final en una NUEVA transacción
            service.completarMantenimiento(solicitudId, resultado);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
