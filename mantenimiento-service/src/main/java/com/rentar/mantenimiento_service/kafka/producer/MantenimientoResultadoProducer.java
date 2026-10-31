package com.rentar.mantenimiento_service.kafka.producer;

import com.rentar.mantenimiento_service.config.KafkaConfig;
import com.rentar.mantenimiento_service.kafka.event.MantenimientoCompletadoInternalEvent;
import com.rentar.mantenimiento_service.kafka.event.MantenimientoResultadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica en Kafka el resultado de un mantenimiento confirmado.
 *
 * <p>Escucha el evento interno únicamente después del commit de la
 * transacción para evitar comunicar resultados de solicitudes que no hayan
 * quedado persistidas.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MantenimientoResultadoProducer {

    private final KafkaTemplate<String, MantenimientoResultadoEvent> kafkaTemplate;

    /**
     * GARANTÍA DE PUBLICACIÓN (Transactional Outbox Pattern):
     * Este método SOLO se ejecuta DESPUÉS de que la transacción en MySQL haya hecho COMMIT exitoso.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicarResultadoEnKafka(MantenimientoCompletadoInternalEvent internalEvent) {
        if (internalEvent == null || internalEvent.getReservaId() == null) {
            log.error("[KAFKA PRODUCER] Evento interno nulo o sin reservaId");
            return;
        }

        MantenimientoResultadoEvent eventSalida = new MantenimientoResultadoEvent(
                internalEvent.getReservaId(),
                internalEvent.getVehiculoId(),
                internalEvent.getResultado()
        );

        String key = internalEvent.getReservaId().toString();
        kafkaTemplate.send(KafkaConfig.TOPIC_MANTENIMIENTO_RESULTADO, key, eventSalida)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[KAFKA PRODUCER] Error enviando resultado a Kafka para reservaId={}: {}",
                                internalEvent.getReservaId(), ex.getMessage(), ex);
                    } else {
                        log.info("[KAFKA PRODUCER] Evento mantenimiento.resultado publicado exitosamente para reservaId={}: {}",
                                internalEvent.getReservaId(), eventSalida);
                    }
                });
    }
}
