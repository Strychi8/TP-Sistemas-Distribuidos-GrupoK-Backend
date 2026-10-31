package com.rentar.vehicle_service.service;

import com.rentar.vehicle_service.dto.request.VehiculoRequestDTO;
import com.rentar.vehicle_service.enums.TipoVehiculo;
import com.rentar.vehicle_service.kafka.event.VerificacionVehiculoEvent;
import com.rentar.vehicle_service.mapper.VehiculoMapper;
import com.rentar.vehicle_service.model.Vehiculo;
import com.rentar.vehicle_service.repository.IVehiculoRepository;
import com.rentar.vehicle_service.service.impl.VehiculoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceImplTest {

    @Mock
    private IVehiculoRepository vehiculoRepository;

    @Mock
    private VehiculoMapper vehiculoMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private VehiculoServiceImpl vehiculoService;

    @BeforeEach
    void setUp() {
        vehiculoService = new VehiculoServiceImpl(vehiculoRepository, vehiculoMapper, eventPublisher);
    }

    @Test
    @DisplayName("crearVehiculo guarda el vehículo y publica el evento de verificación")
    void testCrearVehiculoPublicaEvento() {
        VehiculoRequestDTO requestDTO = new VehiculoRequestDTO();
        requestDTO.setPatente("ABC123");
        requestDTO.setMarca("Toyota");
        requestDTO.setModelo("Corolla");
        requestDTO.setAnio(2022);
        requestDTO.setTipoVehiculo(TipoVehiculo.SEDAN);
        requestDTO.setPrecioDiario(BigDecimal.valueOf(50.0));

        Vehiculo vehiculoEntity = Vehiculo.builder()
                .idVehiculo(10L)
                .patente("ABC123")
                .build();

        when(vehiculoMapper.toEntity(any())).thenReturn(vehiculoEntity);
        when(vehiculoRepository.save(any())).thenReturn(vehiculoEntity);
        when(vehiculoMapper.toDTO(any())).thenReturn(null); // No importa el DTO para este test

        vehiculoService.crearVehiculo(requestDTO);

        ArgumentCaptor<VerificacionVehiculoEvent> eventCaptor = ArgumentCaptor.forClass(VerificacionVehiculoEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        VerificacionVehiculoEvent published = eventCaptor.getValue();
        assertEquals(10L, published.getVehiculoId());
        assertEquals("ABC123", published.getPatente());
    }

    @Test
    @DisplayName("crearVehiculo con conflicto (patente duplicada) lanza excepción y NO publica evento")
    void testCrearVehiculoRollbackNoPublicaEvento() {
        VehiculoRequestDTO requestDTO = new VehiculoRequestDTO();
        requestDTO.setPatente("ABC123");

        when(vehiculoMapper.toEntity(any())).thenReturn(new Vehiculo());
        when(vehiculoRepository.save(any())).thenThrow(new DataIntegrityViolationException("Duplicate"));

        assertThrows(RuntimeException.class, () -> vehiculoService.crearVehiculo(requestDTO));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("eliminarVehiculo realiza baja lógica (activo=false) pero deja inhabilitado intacto")
    void testEliminarVehiculoNoTocaInhabilitado() {
        Vehiculo vehiculo = Vehiculo.builder()
                .idVehiculo(5L)
                .activo(true)
                .inhabilitado(true) // Supongamos que estaba inhabilitado
                .build();

        when(vehiculoRepository.findById(5L)).thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        vehiculoService.eliminarVehiculo(5L);

        assertFalse(vehiculo.getActivo());
        assertTrue(vehiculo.getInhabilitado(), "La baja lógica no debe modificar el campo inhabilitado");
        verify(vehiculoRepository).save(vehiculo);
    }
}
