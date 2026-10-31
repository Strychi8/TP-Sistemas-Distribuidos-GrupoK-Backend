package com.rentar.vehicle_service.kafka.producer;

import com.rentar.vehicle_service.kafka.event.VehiculoExternoCompletadaEvent;
import com.rentar.vehicle_service.kafka.event.VehiculoExternoRegistradoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class VehiculoExternoProducer {
    public static final String TOPIC_EXTERNOS_REGISTRADOS = "vehiculos.externos.registrados";
    public static final String TOPIC_EXTERNOS_COMPLETADA = "vehiculos.externos.validacion.completada";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void enviarRegistrado(VehiculoExternoRegistradoEvent event) {
        kafkaTemplate.send(TOPIC_EXTERNOS_REGISTRADOS, event.getVehiculoExternoId().toString(), event);
    }

    public void enviarCompletada(VehiculoExternoCompletadaEvent event) {
        kafkaTemplate.send(TOPIC_EXTERNOS_COMPLETADA, event.getVehiculoExternoId().toString(), event);
    }
}
