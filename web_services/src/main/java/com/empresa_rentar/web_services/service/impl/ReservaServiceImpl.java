package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.dto.request.FiltroReservaDTO;
import com.empresa_rentar.web_services.dto.response.ReservaClienteDTO;
import com.empresa_rentar.web_services.dto.response.ReservaResponseDTO;
import com.empresa_rentar.web_services.dto.response.VehiculoResponseDTO;
import com.empresa_rentar.web_services.enums.EstadoReserva;
import com.empresa_rentar.web_services.enums.EstadoVehiculo;
import com.empresa_rentar.web_services.enums.TipoVehiculo;
import com.empresa_rentar.web_services.service.IReservaService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaServiceImpl implements IReservaService {

    private final List<ReservaResponseDTO> mockReservas;

    public ReservaServiceImpl() {
        this.mockReservas = new ArrayList<>();
        
        // Mock Cliente 1
        ReservaClienteDTO cliente1 = ReservaClienteDTO.builder()
                .idCliente(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .dni("12345678")
                .email("juan@ejemplo.com")
                .build();
                
        // Mock Cliente 2
        ReservaClienteDTO cliente2 = ReservaClienteDTO.builder()
                .idCliente(2L)
                .nombre("María")
                .apellido("Gómez")
                .dni("87654321")
                .email("maria@ejemplo.com")
                .build();

        // Mock Vehiculo 1
        VehiculoResponseDTO vehiculo1 = VehiculoResponseDTO.builder()
                .idVehiculo(1L)
                .patente("AB123CD")
                .marca("Toyota")
                .modelo("Corolla")
                .anio(2022)
                .color("Blanco")
                .tipoVehiculo(TipoVehiculo.SEDAN)
                .precioDiario(BigDecimal.valueOf(15000))
                .estado(EstadoVehiculo.RESERVADO)
                .activo(true)
                .build();

        // Mock Vehiculo 2
        VehiculoResponseDTO vehiculo2 = VehiculoResponseDTO.builder()
                .idVehiculo(2L)
                .patente("XYZ987")
                .marca("Ford")
                .modelo("Ranger")
                .anio(2023)
                .color("Negro")
                .tipoVehiculo(TipoVehiculo.PICKUP)
                .precioDiario(BigDecimal.valueOf(25000))
                .estado(EstadoVehiculo.EN_ALQUILER)
                .activo(true)
                .build();

        // Reserva 1
        mockReservas.add(ReservaResponseDTO.builder()
                .idReserva(101L)
                .cliente(cliente1)
                .vehiculo(vehiculo1)
                .fechaInicio("2026-10-01T10:00:00")
                .fechaFin("2026-10-05T10:00:00")
                .precioDiario(BigDecimal.valueOf(15000))
                .importeTotal(BigDecimal.valueOf(60000))
                .estado(EstadoReserva.CONFIRMADA)
                .build());

        // Reserva 2
        mockReservas.add(ReservaResponseDTO.builder()
                .idReserva(102L)
                .cliente(cliente2)
                .vehiculo(vehiculo2)
                .fechaInicio("2026-09-15T08:00:00")
                .fechaFin("2026-09-20T08:00:00")
                .precioDiario(BigDecimal.valueOf(25000))
                .importeTotal(BigDecimal.valueOf(125000))
                .estado(EstadoReserva.FINALIZADA)
                .build());
                
        // Reserva 3 (Cancelada)
        mockReservas.add(ReservaResponseDTO.builder()
                .idReserva(103L)
                .cliente(cliente1)
                .vehiculo(vehiculo2)
                .fechaInicio("2026-11-01T10:00:00")
                .fechaFin("2026-11-02T10:00:00")
                .precioDiario(BigDecimal.valueOf(25000))
                .importeTotal(BigDecimal.valueOf(25000))
                .estado(EstadoReserva.CANCELADA)
                .build());
    }

    @Override
    public List<ReservaResponseDTO> consultarReservas(FiltroReservaDTO filtro) {
        if (filtro == null) {
            return mockReservas;
        }

        return mockReservas.stream().filter(reserva -> {
            if (filtro.getIdCliente() != null && !reserva.getCliente().getIdCliente().equals(filtro.getIdCliente())) {
                return false;
            }
            if (filtro.getIdVehiculo() != null && !reserva.getVehiculo().getIdVehiculo().equals(filtro.getIdVehiculo())) {
                return false;
            }
            if (filtro.getTipoVehiculo() != null && reserva.getVehiculo().getTipoVehiculo() != filtro.getTipoVehiculo()) {
                return false;
            }
            if (filtro.getEstado() != null && reserva.getEstado() != filtro.getEstado()) {
                return false;
            }
            if (filtro.getFechaDesde() != null) {
                LocalDateTime filtroDesde = LocalDateTime.parse(filtro.getFechaDesde(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime reservaInicio = LocalDateTime.parse(reserva.getFechaInicio(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                if (reservaInicio.isBefore(filtroDesde)) {
                    return false;
                }
            }
            if (filtro.getFechaHasta() != null) {
                LocalDateTime filtroHasta = LocalDateTime.parse(filtro.getFechaHasta(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime reservaFin = LocalDateTime.parse(reserva.getFechaFin(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                if (reservaFin.isAfter(filtroHasta)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }
}
