package com.rentar.rental_service.kafka.producer;

import com.rentar.rental_service.kafka.event.ReservaConfirmadaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ReservaConfirmadaProducer {

    private static final String TOPIC = "reserva.confirmada";

    private final KafkaTemplate<String, ReservaConfirmadaEvent> kafkaTemplate;

    public ReservaConfirmadaProducer(
            KafkaTemplate<String, ReservaConfirmadaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicarReservaConfirmada(ReservaConfirmadaEvent event) {
        kafkaTemplate.send(TOPIC, event.reservaId().toString(), event)
                .whenComplete((resultado, error) -> {
                    if (error != null) {
                        log.error(
                                "Error al publicar la reserva {} en Kafka",
                                event.reservaId(),
                                error
                        );
                    } else {
                        log.info(
                                "Evento reserva.confirmada publicado para la reserva {}",
                                event.reservaId()
                        );
                    }
                });
    }
}