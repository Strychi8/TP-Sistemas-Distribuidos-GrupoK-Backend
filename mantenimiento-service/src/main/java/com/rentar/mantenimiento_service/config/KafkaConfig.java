package com.rentar.mantenimiento_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {
    // Nombres de los tópicos
    public static final String TOPIC_RESERVA_CONFIRMADA = "reserva.confirmada";

    public static final String TOPIC_MANTENIMIENTO_RESULTADO = "mantenimiento.resultado";

    @Bean
    public NewTopic topicReservaConfirmada() {
        return TopicBuilder.name(TOPIC_RESERVA_CONFIRMADA)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicMantenimientoResultado() {
        return TopicBuilder.name(TOPIC_MANTENIMIENTO_RESULTADO)
                .partitions(1)
                .replicas(1)
                .build();
    }


}