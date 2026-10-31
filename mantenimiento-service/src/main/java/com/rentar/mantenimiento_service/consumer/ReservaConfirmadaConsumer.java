package com.rentar.mantenimiento_service.consumer;

import com.rentar.mantenimiento_service.config.KafkaConfig;
import com.rentar.mantenimiento_service.dto.ReservaConfirmadaEvent;
import com.rentar.mantenimiento_service.service.MantenimientoService;
import com.rentar.mantenimiento_service.service.MantenimientoSimuladorService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservaConfirmadaConsumer {

    private final MantenimientoService mantenimientoService;
    private final MantenimientoSimuladorService simuladorService;

    @KafkaListener(topics = KafkaConfig.TOPIC_RESERVA_CONFIRMADA,
            properties = {
                    "spring.json.value.default.type=com.rentar.mantenimiento_service.dto.ReservaConfirmadaEvent"
            })
    public void consumirReservaConfirmada(ReservaConfirmadaEvent event) {
        try {
            Long solicitudId = mantenimientoService.crearSolicitudMantenimiento(event);

            simuladorService.simularYFinalizarMantenimiento(solicitudId);

        } catch (DataIntegrityViolationException e) {
            System.out.println("Solicitud duplicada ignorada para reservaID: " + event.getReservaId());
        }
    }
}
