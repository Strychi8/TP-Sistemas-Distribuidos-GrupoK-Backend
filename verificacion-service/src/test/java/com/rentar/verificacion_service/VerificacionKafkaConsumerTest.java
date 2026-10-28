package com.rentar.verificacion_service;

import com.rentar.verificacion_service.kafka.event.*;
import com.rentar.verificacion_service.kafka.consumer.VerificacionKafkaConsumer;
import com.rentar.verificacion_service.kafka.producer.VerificacionKafkaProducer;
import com.rentar.verificacion_service.service.IRegistroPolicialService;
import com.rentar.verificacion_service.service.IRegistroTitularidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacionKafkaConsumerTest {

    @Mock
    private IRegistroPolicialService policialService;

    @Mock
    private IRegistroTitularidadService titularidadService;

    @Mock
    private VerificacionKafkaProducer producer;

    private VerificacionKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new VerificacionKafkaConsumer(policialService, titularidadService, producer);
    }

    @Test
    @DisplayName("Caso 1: Patente limpia -> Publica VALIDADO")
    void testVerificacionSolicitadaValida() {
        VerificacionVehiculoEvent event = new VerificacionVehiculoEvent(15L, "ABC123");
        when(policialService.esRobado("ABC123")).thenReturn(false);

        consumer.consumirVerificacionSolicitada(event);

        ArgumentCaptor<VerificacionResultadoEvent> captor = ArgumentCaptor.forClass(VerificacionResultadoEvent.class);
        verify(producer).publicarResultadoVerificacion(captor.capture());

        VerificacionResultadoEvent emitted = captor.getValue();
        assertEquals(15L, emitted.getVehiculoId());
        assertEquals("ABC123", emitted.getPatente());
        assertEquals("VALIDADO", emitted.getResultado());
    }

    @Test
    @DisplayName("Caso 1: Patente robada -> Publica ROBADO")
    void testVerificacionSolicitadaRobada() {
        VerificacionVehiculoEvent event = new VerificacionVehiculoEvent(20L, "ROB123");
        when(policialService.esRobado("ROB123")).thenReturn(true);

        consumer.consumirVerificacionSolicitada(event);

        ArgumentCaptor<VerificacionResultadoEvent> captor = ArgumentCaptor.forClass(VerificacionResultadoEvent.class);
        verify(producer).publicarResultadoVerificacion(captor.capture());

        VerificacionResultadoEvent emitted = captor.getValue();
        assertEquals(20L, emitted.getVehiculoId());
        assertEquals("ROB123", emitted.getPatente());
        assertEquals("ROBADO", emitted.getResultado());
    }

    @Test
    @DisplayName("Caso 2: Vehiculo externo registrado con patente formato viejo (DEF456) -> Dispara ambas validaciones")
    void testVehiculoExternoRegistradoFormatoViejo() {
        VehiculoExternoRegistradoEvent event = new VehiculoExternoRegistradoEvent(50L, 89L, "30123456", "DEF456");

        when(policialService.esRobado("DEF456")).thenReturn(false);
        when(titularidadService.verificarTitularidad(89L, "30123456", "DEF456"))
                .thenReturn(new IRegistroTitularidadService.ResultadoTitularidad(true, "30123456", "Ana Martinez"));

        consumer.consumirVehiculoExternoRegistrado(event);

        // Validar evento de patente
        ArgumentCaptor<ValidacionPatenteEvent> patCaptor = ArgumentCaptor.forClass(ValidacionPatenteEvent.class);
        verify(producer).publicarValidacionPatente(patCaptor.capture());
        assertEquals("VALIDADO", patCaptor.getValue().getResultado());
        assertEquals("DEF456", patCaptor.getValue().getPatente());

        // Validar evento de titularidad
        ArgumentCaptor<ValidacionTitularidadEvent> titCaptor = ArgumentCaptor.forClass(ValidacionTitularidadEvent.class);
        verify(producer).publicarValidacionTitularidad(titCaptor.capture());
        assertEquals("VALIDADO", titCaptor.getValue().getResultado());
        assertEquals("30123456", titCaptor.getValue().getDocumento());
    }

    @Test
    @DisplayName("Caso 3: Si falla tecnicamente la titularidad, igual se publica validacion.patente y titularidad=RECHAZADO")
    void testVehiculoExternoFalloTecnicoTitularidad() {
        VehiculoExternoRegistradoEvent event = new VehiculoExternoRegistradoEvent(50L, 89L, "30123456", "DEF456");

        when(policialService.esRobado("DEF456")).thenReturn(false);
        when(titularidadService.verificarTitularidad(any(), any(), any()))
                .thenThrow(new RuntimeException("Simulacion de fallo en base de datos"));

        consumer.consumirVehiculoExternoRegistrado(event);

        // La validacion de patente no debe verse afectada
        ArgumentCaptor<ValidacionPatenteEvent> patCaptor = ArgumentCaptor.forClass(ValidacionPatenteEvent.class);
        verify(producer).publicarValidacionPatente(patCaptor.capture());
        assertEquals("VALIDADO", patCaptor.getValue().getResultado());

        // La titularidad debe publicarse como RECHAZADO para no dejar el flujo colgado
        ArgumentCaptor<ValidacionTitularidadEvent> titCaptor = ArgumentCaptor.forClass(ValidacionTitularidadEvent.class);
        verify(producer).publicarValidacionTitularidad(titCaptor.capture());
        assertEquals("RECHAZADO", titCaptor.getValue().getResultado());
        assertEquals(50L, titCaptor.getValue().getVehiculoExternoId());
    }

    @Test
    @DisplayName("Caso 4: verificacion-service NO debe publicar evento 'completada' (responsabilidad de vehicle-service)")
    void testNoPublicaCompletadaDesdeVerificacionService() {
        VehiculoExternoRegistradoEvent event = new VehiculoExternoRegistradoEvent(50L, 89L, "30123456", "DEF456");

        when(policialService.esRobado("DEF456")).thenReturn(false);
        when(titularidadService.verificarTitularidad(89L, "30123456", "DEF456"))
                .thenReturn(new IRegistroTitularidadService.ResultadoTitularidad(true, "30123456", "Ana Martinez"));

        consumer.consumirVehiculoExternoRegistrado(event);

        // Solo debe publicar en patente y en titularidad, ninguna otra interaccion
        verify(producer, times(1)).publicarValidacionPatente(any());
        verify(producer, times(1)).publicarValidacionTitularidad(any());
        verifyNoMoreInteractions(producer);
    }

    @Test
    @DisplayName("Caso 5: Vehiculo externo con patente formato Mercosur (AA111BB) denunciada como robada")
    void testVehiculoExternoPatenteMercosurRobada() {
        VehiculoExternoRegistradoEvent event = new VehiculoExternoRegistradoEvent(60L, 89L, "30123456", "AA111BB");

        when(policialService.esRobado("AA111BB")).thenReturn(true);
        when(titularidadService.verificarTitularidad(89L, "30123456", "AA111BB"))
                .thenReturn(new IRegistroTitularidadService.ResultadoTitularidad(true, "30123456", "Ana Martinez"));

        consumer.consumirVehiculoExternoRegistrado(event);

        ArgumentCaptor<ValidacionPatenteEvent> patCaptor = ArgumentCaptor.forClass(ValidacionPatenteEvent.class);
        verify(producer).publicarValidacionPatente(patCaptor.capture());
        assertEquals("RECHAZADO", patCaptor.getValue().getResultado());
        assertEquals("AA111BB", patCaptor.getValue().getPatente());

        ArgumentCaptor<ValidacionTitularidadEvent> titCaptor = ArgumentCaptor.forClass(ValidacionTitularidadEvent.class);
        verify(producer).publicarValidacionTitularidad(titCaptor.capture());
        assertEquals("VALIDADO", titCaptor.getValue().getResultado());
    }
}
