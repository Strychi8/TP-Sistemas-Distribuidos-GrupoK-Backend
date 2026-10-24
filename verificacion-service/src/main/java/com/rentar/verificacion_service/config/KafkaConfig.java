package com.rentar.verificacion_service.config;

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
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    // Nombres de los topicos
    public static final String TOPIC_VERIFICACION_SOLICITADA = "vehiculos.verificacion.solicitada";
    public static final String TOPIC_VERIFICACION_RESULTADO = "vehiculos.verificacion.resultado";
    public static final String TOPIC_EXTERNOS_REGISTRADOS = "vehiculos.externos.registrados";
    public static final String TOPIC_EXTERNOS_VALIDACION_PATENTE = "vehiculos.externos.validacion.patente";
    public static final String TOPIC_EXTERNOS_VALIDACION_TITULARIDAD = "vehiculos.externos.validacion.titularidad";

    @Value("${spring.kafka.bootstrap-servers:localhost:9094}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:verificacion-service-group}")
    private String groupId;

    // ==========================================
    // 1. Creacion automatica de topicos en Kafka
    // ==========================================
    @Bean
    public NewTopic topicVerificacionSolicitada() {
        return TopicBuilder.name(TOPIC_VERIFICACION_SOLICITADA)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicVerificacionResultado() {
        return TopicBuilder.name(TOPIC_VERIFICACION_RESULTADO)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicExternosRegistrados() {
        return TopicBuilder.name(TOPIC_EXTERNOS_REGISTRADOS)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicExternosValidacionPatente() {
        return TopicBuilder.name(TOPIC_EXTERNOS_VALIDACION_PATENTE)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicExternosValidacionTitularidad() {
        return TopicBuilder.name(TOPIC_EXTERNOS_VALIDACION_TITULARIDAD)
                .partitions(1)
                .replicas(1)
                .build();
    }

    // ==========================================
    // 2. Configuracion del Producer
    // ==========================================
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Evita agregar metadatos de clase Java en los headers para maxima interoperabilidad
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ==========================================
    // 3. Configuracion del Consumer
    // ==========================================
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // Acepta cualquier paquete y no falla por nombres de clases de otros microservicios
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // Facilita la conversión JSON a DTOs de Spring
        factory.setRecordMessageConverter(new StringJsonMessageConverter());
        return factory;
    }
}
