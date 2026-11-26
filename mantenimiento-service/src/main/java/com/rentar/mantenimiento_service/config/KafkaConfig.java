package com.rentar.mantenimiento_service.config;

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
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

/** Configura la conexión Kafka y los formatos de mensajes del servicio. */
@Configuration
@EnableKafka
public class KafkaConfig {

    /** Topic que recibe los eventos de reserva confirmada. */
    public static final String TOPIC_RESERVA_CONFIRMADA = "reserva.confirmada";
    /** Topic donde se publican los resultados del mantenimiento. */
    public static final String TOPIC_MANTENIMIENTO_RESULTADO = "mantenimiento.resultado";

    /** Dirección del broker Kafka. */
    @Value("${spring.kafka.bootstrap-servers:localhost:29092}")
    private String bootstrapServers;

    /** Grupo al que se suscriben los consumidores del servicio. */
    @Value("${spring.kafka.consumer.group-id:mantenimiento-service-group}")
    private String groupId;

    /** Declara el topic de reservas confirmadas. */
    @Bean
    public NewTopic topicReservaConfirmada() {
        return TopicBuilder.name(TOPIC_RESERVA_CONFIRMADA)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /** Declara el topic de resultados de mantenimiento. */
    @Bean
    public NewTopic topicMantenimientoResultado() {
        return TopicBuilder.name(TOPIC_MANTENIMIENTO_RESULTADO)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /** Configura la serialización de claves de texto y valores JSON. */
    @Bean
    public ProducerFactory<String, Object> producerFactory(JsonMapper jsonMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        JacksonJsonSerializer<Object> valueSerializer = new JacksonJsonSerializer<>(jsonMapper).noTypeInfo();
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), valueSerializer);
    }

    /** Expone el productor que publica mensajes en Kafka. */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /** Convierte los mensajes JSON recibidos a objetos Java. */
    @Bean
    public RecordMessageConverter recordMessageConverter(JsonMapper jsonMapper) {
        return new StringJacksonJsonMessageConverter(jsonMapper);
    }

    /** Configura los listeners Kafka con el grupo y los conversores del servicio. */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            RecordMessageConverter recordMessageConverter) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class);

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.setRecordMessageConverter(recordMessageConverter);
        return factory;
    }
}
