package com.rentar.vehicle_service.kafka.producer;

import com.rentar.vehicle_service.kafka.event.VerificacionVehiculoEvent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class VehiculoVerificacionProducer {
    public static final String TOPIC_VERIFICACION_SOLICITADA = "vehiculos.verificacion.solicitada";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void enviar(VerificacionVehiculoEvent event) {
        kafkaTemplate.send(TOPIC_VERIFICACION_SOLICITADA, event.getVehiculoId().toString(), event);
    }
}
