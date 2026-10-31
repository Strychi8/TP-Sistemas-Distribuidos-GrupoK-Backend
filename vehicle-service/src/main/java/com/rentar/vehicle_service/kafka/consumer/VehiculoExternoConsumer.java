package com.rentar.vehicle_service.kafka.consumer;

import com.rentar.vehicle_service.kafka.event.ValidacionPatenteEvent;
import com.rentar.vehicle_service.kafka.event.ValidacionTitularidadEvent;
import com.rentar.vehicle_service.kafka.event.VehiculoExternoCompletadaEvent;
import com.rentar.vehicle_service.service.IVehiculoExternoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehiculoExternoConsumer {

    private final IVehiculoExternoService vehiculoExternoService;

    @KafkaListener(topics = "vehiculos.externos.validacion.patente", containerFactory = "externosKafkaListenerContainerFactory")
    public void onValidacionPatente(ValidacionPatenteEvent event) {
        log.info("Recibido evento validación patente: externId={}, resultado={}", event.getVehiculoExternoId(), event.getResultado());
        vehiculoExternoService.procesarValidacionPatente(event);
    }

    @KafkaListener(topics = "vehiculos.externos.validacion.titularidad", containerFactory = "externosKafkaListenerContainerFactory")
    public void onValidacionTitularidad(ValidacionTitularidadEvent event) {
        log.info("Recibido evento validación titularidad: externId={}, resultado={}", event.getVehiculoExternoId(), event.getResultado());
        vehiculoExternoService.procesarValidacionTitularidad(event);
    }

    @KafkaListener(topics = "vehiculos.externos.validacion.completada", containerFactory = "externosKafkaListenerContainerFactory")
    public void onCompletada(VehiculoExternoCompletadaEvent event) {
        log.info("Recibido evento completada externo: externId={}", event.getVehiculoExternoId());
        vehiculoExternoService.procesarCompletada(event);
    }
}
