package com.rentar.mantenimiento_service;

import com.rentar.mantenimiento_service.config.KafkaConfig;
import com.rentar.mantenimiento_service.kafka.event.MantenimientoCompletadoInternalEvent;
import com.rentar.mantenimiento_service.kafka.event.MantenimientoResultadoEvent;
import com.rentar.mantenimiento_service.kafka.producer.MantenimientoResultadoProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MantenimientoResultadoProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private MantenimientoResultadoProducer producer;

    @Test
    @DisplayName("publicarResultadoEnKafka envía mensaje a tópico mantenimiento.resultado con clave reservaId")
    void publicarResultadoEnKafka_Exito() {
        MantenimientoCompletadoInternalEvent internalEvent =
                new MantenimientoCompletadoInternalEvent(150L, 15L, "REALIZADO");

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq(KafkaConfig.TOPIC_MANTENIMIENTO_RESULTADO), eq("150"), any(MantenimientoResultadoEvent.class)))
                .thenReturn(future);

        producer.publicarResultadoEnKafka(internalEvent);

        ArgumentCaptor<MantenimientoResultadoEvent> captor = ArgumentCaptor.forClass(MantenimientoResultadoEvent.class);
        verify(kafkaTemplate).send(eq(KafkaConfig.TOPIC_MANTENIMIENTO_RESULTADO), eq("150"), captor.capture());

        MantenimientoResultadoEvent salida = captor.getValue();
        assertThat(salida.getReservaId()).isEqualTo(150L);
        assertThat(salida.getVehiculoId()).isEqualTo(15L);
        assertThat(salida.getResultado()).isEqualTo("REALIZADO");
    }

    @Test
    @DisplayName("publicarResultadoEnKafka con evento nulo o sin reservaId no publica mensaje")
    void publicarResultadoEnKafka_Nulo_NoPublica() {
        producer.publicarResultadoEnKafka(null);
        producer.publicarResultadoEnKafka(new MantenimientoCompletadoInternalEvent(null, 15L, "REALIZADO"));

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
