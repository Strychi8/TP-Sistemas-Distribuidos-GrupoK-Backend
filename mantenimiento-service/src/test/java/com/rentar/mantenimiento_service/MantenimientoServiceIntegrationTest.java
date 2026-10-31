package com.rentar.mantenimiento_service;


import com.rentar.mantenimiento_service.config.KafkaConfig;
import com.rentar.mantenimiento_service.kafka.event.ReservaConfirmadaEvent;
import com.rentar.mantenimiento_service.model.EstadoMantenimiento;
import com.rentar.mantenimiento_service.model.SolicitudMantenimiento;
import com.rentar.mantenimiento_service.repository.SolicitudMantenimientoRepository;
import com.rentar.mantenimiento_service.service.MantenimientoService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
        "spring.kafka.consumer.group-id=mantenimiento-service-integration-test",
        "server.port=0",
        "spring.main.allow-bean-definition-overriding=true"
})
@EmbeddedKafka(
        partitions = 1,
        topics = {KafkaConfig.TOPIC_RESERVA_CONFIRMADA, KafkaConfig.TOPIC_MANTENIMIENTO_RESULTADO},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@ActiveProfiles("test")
class MantenimientoServiceIntegrationTest {

    @TestConfiguration
    static class KafkaTestProducerConfiguration {

        @Bean
        @Primary
        KafkaTemplate<String, Object> kafkaTestTemplate(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

            ProducerFactory<String, Object> producerFactory =
                    new DefaultKafkaProducerFactory<>(
                            properties,
                            new StringSerializer(),
                            new JacksonJsonSerializer<>()
                    );
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean(name = "kafkaListenerContainerFactory")
        ConcurrentKafkaListenerContainerFactory<String, ReservaConfirmadaEvent> kafkaTestListenerContainerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                @Value("${spring.kafka.consumer.group-id}") String groupId) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            ConsumerFactory<String, ReservaConfirmadaEvent> consumerFactory =
                    new DefaultKafkaConsumerFactory<>(
                            properties,
                            new ErrorHandlingDeserializer<>(new StringDeserializer()),
                            new ErrorHandlingDeserializer<>(
                                    new JacksonJsonDeserializer<>(
                                            ReservaConfirmadaEvent.class, false
                                    )
                            )
                    );

            ConcurrentKafkaListenerContainerFactory<String, ReservaConfirmadaEvent> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            return factory;
        }
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private MantenimientoService mantenimientoService;

    @Autowired
    private SolicitudMantenimientoRepository repository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    void setUp() {
        // Garantiza el aislamiento de cada test limpiando la base de datos
        repository.deleteAll();
    }

    @Test
    @DisplayName("1. Consumo de 'reserva.confirmada' y finalización del mantenimiento")
    void test1_ConsumoReservaConfirmada() {
        ReservaConfirmadaEvent event = new ReservaConfirmadaEvent(
                101L, 15L, LocalDateTime.now(), LocalDateTime.now().plusDays(3)
        );

        // Publicamos el evento al tópico de kafka embebido
        kafkaTemplate.send(KafkaConfig.TOPIC_RESERVA_CONFIRMADA, event);

        // Awaitility: Espera asíncrona a que el @KafkaListener procese el mensaje
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Optional<SolicitudMantenimiento> solicitud = repository.findByReservaId(101L);
            assertThat(solicitud).isPresent();
            assertThat(solicitud.get().getVehiculoId()).isEqualTo(15L);
            assertThat(solicitud.get().getEstado()).isEqualTo(EstadoMantenimiento.REALIZADO);
        });
    }

    @Test
    @DisplayName("2. La reentrega del mismo evento no debe duplicar la solicitud en BD")
    void test2_ReentregaNoDuplicaRegistro() {
        ReservaConfirmadaEvent event = new ReservaConfirmadaEvent(
                102L, 20L, LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );

        // Se envía el mismo evento dos veces a kafka
        kafkaTemplate.send(KafkaConfig.TOPIC_RESERVA_CONFIRMADA, event);
        kafkaTemplate.send(KafkaConfig.TOPIC_RESERVA_CONFIRMADA, event);

        // Verificamos que solo exista un registro en la base de datos
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(repository.count()).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("3. Completar simulación y publicar evento 'mantenimiento.resultado' (REALIZADO)")
    void test3_ProcesamientoYPublicaciónRealizado() {
        Consumer<String, Object> consumerAuxiliar = crearConsumidorPrueba(KafkaConfig.TOPIC_MANTENIMIENTO_RESULTADO);

        ReservaConfirmadaEvent event = new ReservaConfirmadaEvent(
                103L, 25L, LocalDateTime.now(), LocalDateTime.now().plusDays(1)
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_RESERVA_CONFIRMADA, event);

        // Esperamos la ejecución asíncrona (@Async)
        await().atMost(Duration.ofSeconds(6)).untilAsserted(() -> {
            Optional<SolicitudMantenimiento> solicitud = repository.findByReservaId(103L);
            assertThat(solicitud).isPresent();
            assertThat(solicitud.get().getEstado()).isEqualTo(EstadoMantenimiento.REALIZADO);
        });

        // Verificamos que el evento haya sido publicado al tópico de salida en kafka
        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(
                consumerAuxiliar, Duration.ofSeconds(10)
        );
        assertThat(records).isNotEmpty();
        consumerAuxiliar.close();
    }

    @Test
    @DisplayName("4. Guardar y publicar resultado cuando el estado es NO_REALIZADO")
    void test4_ResultadoNoRealizado() {
        Consumer<String, Object> consumerAuxiliar = crearConsumidorPrueba(KafkaConfig.TOPIC_MANTENIMIENTO_RESULTADO);

        ReservaConfirmadaEvent event = new ReservaConfirmadaEvent(
                104L, 30L, LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );
        Long solicitudId = mantenimientoService.crearSolicitudMantenimiento(event);

        // Completamos explicitamente con estado NO_REALIZADO
        mantenimientoService.completarMantenimiento(solicitudId, EstadoMantenimiento.NO_REALIZADO);

        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(
                consumerAuxiliar, Duration.ofSeconds(10)
        );
        assertThat(records).isNotEmpty();
        consumerAuxiliar.close();
    }

    @Test
    @DisplayName("5. Inserción concurrente simultánea controlada por la restricción UNIQUE")
    void test5_ConcurrenciaSimultanea() {
        ReservaConfirmadaEvent event = new ReservaConfirmadaEvent(
                105L, 35L, LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );

        var executor = Executors.newFixedThreadPool(2);

        // Ejecutamos dos hilos en paralelo al mismo milisegundo
        CompletableFuture<Void> hilo1 = CompletableFuture.runAsync(() -> {
            try {
                mantenimientoService.crearSolicitudMantenimiento(event);
            } catch (DataIntegrityViolationException ignored) {
            }
        }, executor);

        CompletableFuture<Void> hilo2 = CompletableFuture.runAsync(() -> {
            try {
                mantenimientoService.crearSolicitudMantenimiento(event);
            } catch (DataIntegrityViolationException ignored) {
            }
        }, executor);

        // Esperamos a que ambos hilos terminen
        CompletableFuture.allOf(hilo1, hilo2).join();

        // Verificamos que solo exista un registro en la base de datos
        assertThat(repository.count()).isEqualTo(1L);
        executor.shutdown();
    }

    // Método para crear un consumidor escucha en los tests
    private Consumer<String, Object> crearConsumidorPrueba(String topic) {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                "grupo-test-" + System.nanoTime(), "true", embeddedKafkaBroker
        );
        DefaultKafkaConsumerFactory<String, Object> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        props,
                        new ErrorHandlingDeserializer<>(new StringDeserializer()),
                        new ErrorHandlingDeserializer<>(
                                new JacksonJsonDeserializer<>(Object.class, false)
                        )
                );
        Consumer<String, Object> consumer = consumerFactory.createConsumer();
        consumer.subscribe(List.of(topic));
        await().atMost(Duration.ofSeconds(5)).until(() -> {
            consumer.poll(Duration.ofMillis(100));
            return !consumer.assignment().isEmpty();
        });
        return consumer;

    }
}