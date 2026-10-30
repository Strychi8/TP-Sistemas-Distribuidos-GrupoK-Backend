package com.rentar.mantenimiento_service.publisher;

import com.rentar.mantenimiento_service.config.KafkaConfig;
import com.rentar.mantenimiento_service.dto.MantenimientoResultadoEvent;
import com.rentar.mantenimiento_service.event.MantenimientoCompletadoInternalEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@AllArgsConstructor
@Slf4j
public class MantenimientoResultadoPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * GARANTÍA DE PUBLICAIÓN (Transactional Outbox Pattern):
     * Este método SOLO se ejecuta DESPUÉS de que la transacción en MySQL haya hecho COMMIT exitoso.
     *
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicarResultadoEnKafka(MantenimientoCompletadoInternalEvent internalEvent) {
        MantenimientoResultadoEvent eventSalida = new MantenimientoResultadoEvent(
                internalEvent.getReservaId(),
                internalEvent.getVehiculoId(),
                internalEvent.getResultado()
        );

        kafkaTemplate.send(KafkaConfig.TOPIC_MANTENIMIENTO_RESULTADO, eventSalida);
        log.info("Evento enviado a kafka [{}] para reservaId: {}", KafkaConfig.TOPIC_MANTENIMIENTO_RESULTADO, internalEvent.getReservaId());

    }
}
