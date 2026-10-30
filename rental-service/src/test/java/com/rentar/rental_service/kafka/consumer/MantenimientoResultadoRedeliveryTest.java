package com.rentar.rental_service.kafka.consumer;

import com.rentar.rental_service.kafka.event.MantenimientoResultadoEvent;
import com.rentar.rental_service.model.Reserva;
import com.rentar.rental_service.repository.IReservaRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(
        classes = MantenimientoResultadoRedeliveryTest.TestConfig.class
)
@EmbeddedKafka(
        partitions = 1,
        topics = "mantenimiento.resultado",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class MantenimientoResultadoRedeliveryTest {

    private static final String TOPIC = "mantenimiento.resultado";

    @Autowired
    private KafkaTemplate<String, MantenimientoResultadoEvent> kafkaTemplate;

    @MockitoBean
    private IReservaRepository reservaRepository;

    @Test
    void recibirDosVecesElMismoResultadoNoDebeGuardarDosVeces()
            throws Exception {

        Long reservaId = 1L;

        Reserva reserva = new Reserva();
        reserva.setIdReserva(reservaId);
        reserva.setResultadoMantenimiento(null);

        when(reservaRepository.findById(reservaId))
                .thenReturn(Optional.of(reserva));

        MantenimientoResultadoEvent evento =
                new MantenimientoResultadoEvent(
                        reservaId, 10L, "REALIZADO");

        kafkaTemplate.send(TOPIC, reservaId.toString(), evento)
                .get(10, TimeUnit.SECONDS);

        org.awaitility.Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        verify(reservaRepository, times(1))
                                .save(any(Reserva.class))
                );

        kafkaTemplate.send(TOPIC, reservaId.toString(), evento)
                .get(10, TimeUnit.SECONDS);

        org.awaitility.Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    verify(reservaRepository, times(2))
                            .findById(reservaId);
                    verify(reservaRepository, times(1))
                            .save(any(Reserva.class));
                });

        assertEquals("REALIZADO", reserva.getResultadoMantenimiento());
    }

    @Configuration
    @EnableKafka
    static class TestConfig {

        @Bean
        MantenimientoResultadoConsumer mantenimientoResultadoConsumer(
                IReservaRepository repository) {
            return new MantenimientoResultadoConsumer(repository);
        }

        @Bean
        ConsumerFactory<String, MantenimientoResultadoEvent>
        testConsumerFactory(
                org.springframework.core.env.Environment environment) {

            Map<String, Object> props = new HashMap<>();
            props.put(
                    org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    environment.getRequiredProperty(
                            "spring.kafka.bootstrap-servers")
            );
            props.put(
                    org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG,
                    "redelivery-test"
            );
            props.put(
                    org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                    "earliest"
            );

            return new DefaultKafkaConsumerFactory<>(
                    props,
                    new org.apache.kafka.common.serialization.StringDeserializer(),
                    new JacksonJsonDeserializer<>(
                            MantenimientoResultadoEvent.class, false)
            );
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, MantenimientoResultadoEvent>
        mantenimientoResultadoKafkaListenerContainerFactory(
                ConsumerFactory<String, MantenimientoResultadoEvent> consumerFactory) {

            var factory =
                    new ConcurrentKafkaListenerContainerFactory<String, MantenimientoResultadoEvent>();
            factory.setConsumerFactory(consumerFactory);
            return factory;
        }

        @Bean
        ProducerFactory<String, MantenimientoResultadoEvent>
        testProducerFactory(
                org.springframework.core.env.Environment environment) {

            Map<String, Object> props = new HashMap<>();
            props.put(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    environment.getRequiredProperty(
                            "spring.kafka.bootstrap-servers")
            );

            return new DefaultKafkaProducerFactory<>(
                    props,
                    new StringSerializer(),
                    new JacksonJsonSerializer<>()
            );
        }

        @Bean
        KafkaTemplate<String, MantenimientoResultadoEvent> kafkaTemplate(
                ProducerFactory<String, MantenimientoResultadoEvent> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }
    }
}
