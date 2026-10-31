package com.rentar.mantenimiento_service.kafka.consumer;

import com.rentar.mantenimiento_service.config.KafkaConfig;
import com.rentar.mantenimiento_service.kafka.event.ReservaConfirmadaEvent;
import com.rentar.mantenimiento_service.service.MantenimientoService;
import com.rentar.mantenimiento_service.service.MantenimientoSimuladorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaConfirmadaConsumer {

    private final MantenimientoService mantenimientoService;
    private final MantenimientoSimuladorService simuladorService;

    @KafkaListener(topics = KafkaConfig.TOPIC_RESERVA_CONFIRMADA)
    public void consumirReservaConfirmada(ReservaConfirmadaEvent event) {
        if (event == null || event.getReservaId() == null || event.getVehiculoId() == null) {
            log.error("[KAFKA CONSUMER] Evento reserva.confirmada nulo o con datos obligatorios faltantes.");
            return;
        }

        log.info("[KAFKA CONSUMER] Recibida confirmacion de reserva para mantenimiento: reservaId={}, vehiculoId={}",
                event.getReservaId(), event.getVehiculoId());

        try {
            Long solicitudId = mantenimientoService.crearSolicitudMantenimiento(event);
            simuladorService.simularYFinalizarMantenimiento(solicitudId);
        } catch (DataIntegrityViolationException e) {
            log.warn("[KAFKA CONSUMER] Solicitud duplicada (idempotencia) ignorada para reservaId: {}",
                    event.getReservaId());
        } catch (Exception e) {
            log.error("[KAFKA CONSUMER] Error inesperado procesando reservaId={}: {}",
                    event.getReservaId(), e.getMessage(), e);
        }
    }
}
