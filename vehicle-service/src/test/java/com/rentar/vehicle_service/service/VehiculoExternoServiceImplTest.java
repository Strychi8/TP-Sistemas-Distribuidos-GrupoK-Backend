package com.rentar.vehicle_service.service;

import com.rentar.vehicle_service.dto.request.VehiculoExternoRequestDTO;
import com.rentar.vehicle_service.dto.response.VehiculoExternoResponseDTO;
import com.rentar.vehicle_service.enums.TipoVehiculo;
import com.rentar.vehicle_service.exception.custom.BadRequestException;
import com.rentar.vehicle_service.exception.custom.ConflictException;
import com.rentar.vehicle_service.kafka.event.ValidacionPatenteEvent;
import com.rentar.vehicle_service.kafka.event.VehiculoExternoCompletadaEvent;
import com.rentar.vehicle_service.kafka.event.VehiculoExternoRegistradoEvent;
import com.rentar.vehicle_service.mapper.VehiculoExternoMapper;
import com.rentar.vehicle_service.model.Vehiculo;
import com.rentar.vehicle_service.model.VehiculoExterno;
import com.rentar.vehicle_service.repository.IVehiculoExternoRepository;
import com.rentar.vehicle_service.repository.IVehiculoRepository;
import com.rentar.vehicle_service.service.impl.VehiculoExternoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoExternoServiceImplTest {

    @Mock
    private IVehiculoExternoRepository vehiculoExternoRepository;

    @Mock
    private IVehiculoRepository vehiculoRepository;

    @Mock
    private VehiculoExternoMapper vehiculoExternoMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private VehiculoExternoServiceImpl vehiculoExternoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Alta externa queda PENDIENTE y publica evento de registro")
    void testRegistrarVehiculoExternoExitoso() {
        VehiculoExternoRequestDTO dto = VehiculoExternoRequestDTO.builder()
                .patente("ABC123")
                .marca("Toyota")
                .modelo("Corolla")
                .anio(2022)
                .color("Blanco")
                .tipoVehiculo(TipoVehiculo.SEDAN)
                .precioDiario(BigDecimal.valueOf(100.00))
                .documento("30123456")
                .clienteId(1L)
                .build();

        VehiculoExterno entidad = VehiculoExterno.builder()
                .idVehiculoExterno(1L)
                .clienteId(1L)
                .patente("ABC123")
                .estado("PENDIENTE")
                .build();

        when(vehiculoRepository.existsByPatente("ABC123")).thenReturn(false);
        when(vehiculoExternoRepository.existsByPatenteAndEstadoIn(eq("ABC123"), any())).thenReturn(false);
        when(vehiculoExternoMapper.toEntity(dto)).thenReturn(entidad);
        when(vehiculoExternoRepository.save(any(VehiculoExterno.class))).thenReturn(entidad);
        when(vehiculoExternoMapper.toDTO(entidad)).thenReturn(VehiculoExternoResponseDTO.builder()
                .idVehiculoExterno(1L)
                .estado("PENDIENTE")
                .build());

        VehiculoExternoResponseDTO response = vehiculoExternoService.registrarVehiculoExterno(dto);

        assertNotNull(response);
        assertEquals("PENDIENTE", response.getEstado());
        verify(eventPublisher).publishEvent(any(VehiculoExternoRegistradoEvent.class));
    }

    @Test
    @DisplayName("Alta externa con precioDiario nulo, 0 o negativo se rechaza con 400")
    void testRegistrarVehiculoPrecioInvalido() {
        VehiculoExternoRequestDTO dto = VehiculoExternoRequestDTO.builder()
                .patente("ABC123")
                .marca("Toyota")
                .modelo("Corolla")
                .anio(2022)
                .tipoVehiculo(TipoVehiculo.SEDAN)
                .precioDiario(BigDecimal.ZERO)
                .documento("30123456")
                .clienteId(1L)
                .build();

        assertThrows(BadRequestException.class, () -> vehiculoExternoService.registrarVehiculoExterno(dto));
    }

    @Test
    @DisplayName("Patente duplicada en flota oficial se rechaza con 409")
    void testRegistrarPatenteDuplicadaOficial() {
        VehiculoExternoRequestDTO dto = VehiculoExternoRequestDTO.builder()
                .patente("ABC123")
                .marca("Toyota")
                .modelo("Corolla")
                .anio(2022)
                .tipoVehiculo(TipoVehiculo.SEDAN)
                .precioDiario(BigDecimal.valueOf(50))
                .documento("30123456")
                .clienteId(1L)
                .build();

        when(vehiculoRepository.existsByPatente("ABC123")).thenReturn(true);

        assertThrows(ConflictException.class, () -> vehiculoExternoService.registrarVehiculoExterno(dto));
    }

    @Test
    @DisplayName("Patente VALIDADO + titularidad VALIDADO -> APROBADO + completada + Vehiculo")
    void testCorrelacionExitosa() {
        VehiculoExterno externo = VehiculoExterno.builder()
                .idVehiculoExterno(1L)
                .patente("AB123CD")
                .marca("Ford")
                .modelo("Focus")
                .anio(2021)
                .tipoVehiculo(TipoVehiculo.HATCHBACK)
                .precioDiario(BigDecimal.valueOf(80))
                .estado("APROBADO")
                .validacionPatente("VALIDADO")
                .validacionTitularidad("VALIDADO")
                .build();

        when(vehiculoExternoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(externo));
        when(vehiculoRepository.findByPatente("AB123CD")).thenReturn(Optional.empty());
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> {
            Vehiculo v = inv.getArgument(0);
            v.setIdVehiculo(10L);
            return v;
        });

        vehiculoExternoService.procesarCompletada(VehiculoExternoCompletadaEvent.builder()
                .vehiculoExternoId(1L)
                .build());

        verify(vehiculoRepository).save(any(Vehiculo.class));
        assertEquals(10L, externo.getVehiculoId());
    }

    @Test
    @DisplayName("Patente RECHAZADO -> RECHAZADO, sin completada, sin Vehiculo")
    void testPatenteRechazada() {
        VehiculoExterno externo = VehiculoExterno.builder()
                .idVehiculoExterno(1L)
                .patente("ABC123")
                .estado("PENDIENTE")
                .build();

        when(vehiculoExternoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(externo));

        vehiculoExternoService.procesarValidacionPatente(ValidacionPatenteEvent.builder()
                .vehiculoExternoId(1L)
                .patente("ABC123")
                .resultado("RECHAZADO")
                .build());

        assertEquals("RECHAZADO", externo.getEstado());
        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }
}
