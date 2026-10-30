package com.rentar.rental_service.config;

import com.rentar.rental_service.kafka.event.MantenimientoResultadoEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, MantenimientoResultadoEvent>
    mantenimientoResultadoConsumerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );
        config.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );
        config.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(
                        MantenimientoResultadoEvent.class,
                        false
                )
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MantenimientoResultadoEvent>
    mantenimientoResultadoKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, MantenimientoResultadoEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                mantenimientoResultadoConsumerFactory()
        );

        return factory;
    }
}