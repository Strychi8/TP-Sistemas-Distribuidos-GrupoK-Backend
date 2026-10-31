package com.rentar.vehicle_service.kafka.consumer;

import com.rentar.vehicle_service.kafka.event.VerificacionResultadoEvent;
import com.rentar.vehicle_service.model.Vehiculo;
import com.rentar.vehicle_service.repository.IVehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoVerificacionConsumerTest {

    @Mock
    private IVehiculoRepository vehiculoRepository;

    private VehiculoVerificacionConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new VehiculoVerificacionConsumer(vehiculoRepository);
    }

    @Test
    @DisplayName("Recibir ROBADO actualiza inhabilitado=true si no lo estaba")
    void testConsumerRobado() {
        VerificacionResultadoEvent event = VerificacionResultadoEvent.builder()
                .vehiculoId(1L)
                .patente("ABC123")
                .resultado("ROBADO")
                .build();

        Vehiculo vehiculo = Vehiculo.builder().idVehiculo(1L).inhabilitado(false).build();
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.updateInhabilitado(1L, true, false)).thenReturn(1);

        consumer.onVerificacionResultado(event);

        verify(vehiculoRepository, times(1)).updateInhabilitado(1L, true, false);
        verify(vehiculoRepository, times(1)).save(vehiculo);
    }

    @Test
    @DisplayName("Recibir VALIDADO actualiza inhabilitado=false solo si no estaba inhabilitado previamente")
    void testConsumerValidado() {
        VerificacionResultadoEvent event = VerificacionResultadoEvent.builder()
                .vehiculoId(1L)
                .patente("ABC123")
                .resultado("VALIDADO")
                .build();

        Vehiculo vehiculo = Vehiculo.builder().idVehiculo(1L).inhabilitado(false).build();
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.updateInhabilitado(1L, false, false)).thenReturn(1);

        consumer.onVerificacionResultado(event);

        verify(vehiculoRepository, times(1)).updateInhabilitado(1L, false, false);
        verify(vehiculoRepository, times(1)).save(vehiculo);
    }

    @Test
    @DisplayName("Recibir VALIDADO posterior a ROBADO no revierte la inhabilitación (idempotencia y seguridad)")
    void testConsumerValidadoNoRevierteRobado() {
        VerificacionResultadoEvent event = VerificacionResultadoEvent.builder()
                .vehiculoId(1L)
                .patente("ABC123")
                .resultado("VALIDADO")
                .build();

        Vehiculo vehiculo = Vehiculo.builder().idVehiculo(1L).inhabilitado(true).build();
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        // El update condicional retorna 0 porque el estado actual es inhabilitado=true (no cumple WHERE inhabilitado = false)
        when(vehiculoRepository.updateInhabilitado(1L, false, false)).thenReturn(0);

        consumer.onVerificacionResultado(event);

        verify(vehiculoRepository, times(1)).updateInhabilitado(1L, false, false);
        // Como dio 0 afectaciones, no debe invocar save() para alterar el estado terminal
        verify(vehiculoRepository, never()).save(any());
    }
}
