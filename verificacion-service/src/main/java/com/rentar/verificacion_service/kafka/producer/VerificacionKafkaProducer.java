package com.rentar.verificacion_service.kafka.producer;

import com.rentar.verificacion_service.config.KafkaConfig;
import com.rentar.verificacion_service.kafka.event.ValidacionPatenteEvent;
import com.rentar.verificacion_service.kafka.event.ValidacionTitularidadEvent;
import com.rentar.verificacion_service.kafka.event.VerificacionResultadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificacionKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Publica el resultado del chequeo policial de un vehiculo recien registrado
    // Topico: vehiculos.verificacion.resultado
    public void publicarResultadoVerificacion(VerificacionResultadoEvent event) {
        log.info("[KAFKA PRODUCER] Publicando en '{}': {}", KafkaConfig.TOPIC_VERIFICACION_RESULTADO, event);
        kafkaTemplate.send(KafkaConfig.TOPIC_VERIFICACION_RESULTADO, String.valueOf(event.getVehiculoId()), event);
    }

    // Publica el resultado de la validacion de patente de un vehiculo externo
    // Topico: vehiculos.externos.validacion.patente
    public void publicarValidacionPatente(ValidacionPatenteEvent event) {
        log.info("[KAFKA PRODUCER] Publicando en '{}': {}", KafkaConfig.TOPIC_EXTERNOS_VALIDACION_PATENTE, event);
        kafkaTemplate.send(KafkaConfig.TOPIC_EXTERNOS_VALIDACION_PATENTE, String.valueOf(event.getVehiculoExternoId()), event);
    }

    // Publica el resultado de la validacion de titularidad de un vehiculo externo
    // Topico: vehiculos.externos.validacion.titularidad
    public void publicarValidacionTitularidad(ValidacionTitularidadEvent event) {
        log.info("[KAFKA PRODUCER] Publicando en '{}': {}", KafkaConfig.TOPIC_EXTERNOS_VALIDACION_TITULARIDAD, event);
        kafkaTemplate.send(KafkaConfig.TOPIC_EXTERNOS_VALIDACION_TITULARIDAD, String.valueOf(event.getVehiculoExternoId()), event);
    }
}
