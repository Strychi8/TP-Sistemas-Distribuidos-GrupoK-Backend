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
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    /** Topics usados para la verificación policial y los vehículos externos. */
    public static final String TOPIC_VERIFICACION_SOLICITADA = "vehiculos.verificacion.solicitada";
    public static final String TOPIC_VERIFICACION_RESULTADO = "vehiculos.verificacion.resultado";
    public static final String TOPIC_EXTERNOS_REGISTRADOS = "vehiculos.externos.registrados";
    public static final String TOPIC_EXTERNOS_VALIDACION_PATENTE = "vehiculos.externos.validacion.patente";
    public static final String TOPIC_EXTERNOS_VALIDACION_TITULARIDAD = "vehiculos.externos.validacion.titularidad";

    /** Dirección del broker Kafka. */
    @Value("${spring.kafka.bootstrap-servers:localhost:29092}")
    private String bootstrapServers;

    /** Grupo de consumidores configurado para el servicio. */
    @Value("${spring.kafka.consumer.group-id:verificacion-service-group}")
    private String groupId;

    /** Declara el topic que recibe solicitudes de verificación policial. */
    @Bean
    public NewTopic topicVerificacionSolicitada() {
        return TopicBuilder.name(TOPIC_VERIFICACION_SOLICITADA).partitions(1).replicas(1).build();
    }

    /** Declara el topic que recibe resultados de verificación policial. */
    @Bean
    public NewTopic topicVerificacionResultado() {
        return TopicBuilder.name(TOPIC_VERIFICACION_RESULTADO).partitions(1).replicas(1).build();
    }

    /** Declara el topic que recibe solicitudes de incorporación externa. */
    @Bean
    public NewTopic topicExternosRegistrados() {
        return TopicBuilder.name(TOPIC_EXTERNOS_REGISTRADOS).partitions(1).replicas(1).build();
    }

    /** Declara el topic con los resultados de validación de patente. */
    @Bean
    public NewTopic topicExternosValidacionPatente() {
        return TopicBuilder.name(TOPIC_EXTERNOS_VALIDACION_PATENTE).partitions(1).replicas(1).build();
    }

    /** Declara el topic con los resultados de validación de titularidad. */
    @Bean
    public NewTopic topicExternosValidacionTitularidad() {
        return TopicBuilder.name(TOPIC_EXTERNOS_VALIDACION_TITULARIDAD).partitions(1).replicas(1).build();
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

    /** Expone el productor Kafka usado para publicar eventos. */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /** Convierte los mensajes JSON recibidos a objetos Java. */
    @Bean
    public RecordMessageConverter recordMessageConverter(JsonMapper jsonMapper) {
        return new StringJacksonJsonMessageConverter(jsonMapper);
    }

    /** Configura los listeners con el grupo de consumidores del servicio. */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            RecordMessageConverter recordMessageConverter) {
        return buildFactory(groupId, recordMessageConverter);
    }

    /** Construye un listener con el grupo y el conversor indicados. */
    private ConcurrentKafkaListenerContainerFactory<String, Object> buildFactory(
            String targetGroupId, RecordMessageConverter converter) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, targetGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class);

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.setRecordMessageConverter(converter);
        return factory;
    }
}
