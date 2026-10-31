package com.rentar.mantenimiento_service.config;

import com.rentar.mantenimiento_service.kafka.event.MantenimientoResultadoEvent;
import com.rentar.mantenimiento_service.kafka.event.ReservaConfirmadaEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configura la comunicación Kafka del dominio de mantenimiento preventivo.
 *
 * <p>Declara los tópicos por los que se reciben confirmaciones de reservas y
 * se publican los resultados del mantenimiento, además de sus productores,
 * consumidores y mecanismos de conversión de mensajes.</p>
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String TOPIC_RESERVA_CONFIRMADA = "reserva.confirmada";
    public static final String TOPIC_MANTENIMIENTO_RESULTADO = "mantenimiento.resultado";

    @Value("${spring.kafka.bootstrap-servers:localhost:29092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:mantenimiento-service-group}")
    private String groupId;

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

    // ==========================================
    // Productor (JsonSerializer sin headers de tipo Java para desacoplar)
    // ==========================================
    @Bean
    public ProducerFactory<String, MantenimientoResultadoEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        configProps.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, MantenimientoResultadoEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ==========================================
    // Consumidor (ErrorHandlingDeserializer + StringJsonMessageConverter)
    // ==========================================
    @Bean
    public ConsumerFactory<String, ReservaConfirmadaEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new ErrorHandlingDeserializer<>(new StringDeserializer()),
                new ErrorHandlingDeserializer<>(
                        new JacksonJsonDeserializer<>(
                                ReservaConfirmadaEvent.class,
                                false
                        )
                ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReservaConfirmadaEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ReservaConfirmadaEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(
                consumerFactory()
        );
        return factory;
    }
}