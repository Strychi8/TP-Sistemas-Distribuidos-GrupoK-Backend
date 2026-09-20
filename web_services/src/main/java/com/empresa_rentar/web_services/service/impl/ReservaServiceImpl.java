package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.dto.request.ReservaRequestDTO;
import com.empresa_rentar.web_services.dto.response.ReservaResponseDTO;
import com.empresa_rentar.web_services.enums.EstadoReserva;
import com.empresa_rentar.web_services.enums.EstadoVehiculo;
import com.empresa_rentar.web_services.exception.custom.BusinessException;
import com.empresa_rentar.web_services.exception.custom.ResourceNotFoundException;
import com.empresa_rentar.web_services.model.Cliente;
import com.empresa_rentar.web_services.model.Reserva;
import com.empresa_rentar.web_services.model.Vehiculo;
import com.empresa_rentar.web_services.repository.IClienteRepository;
import com.empresa_rentar.web_services.repository.IReservaRepository;
import com.empresa_rentar.web_services.repository.IVehiculoRepository;
import com.empresa_rentar.web_services.service.IReservaService;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.empresa_rentar.web_services.mapper.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ReservaServiceImpl implements IReservaService {

    private final IReservaRepository reservaRepository;
    private final IClienteRepository clienteRepository;
    private final IVehiculoRepository vehiculoRepository;
    private final ReservaMapper reservaMapper;

    /**
     * Crea una nueva reserva de alquiler validando todas las reglas de negocio:
     * 1. Las fechas deben ser coherentes (fin > inicio)
     * 2. El cliente debe existir y estar activo
     * 3. El vehículo debe existir y estar activo
     * 4. No debe existir solapamiento de fechas con otra reserva confirmada
     * Calcula el importe total (días × precio diario) y asigna estado CONFIRMADA.
     * Actualiza el estado del vehículo a RESERVADO para bloquear disponibilidad.
     *
     * @param dto datos de la reserva a crear (cliente, vehículo, fechas)
     * @return DTO con la reserva creada e importes calculados
     */
    @Override
    @Transactional
    public ReservaResponseDTO crearReserva(ReservaRequestDTO dto){
        // 1. Validar que la fechaFin sea posterior a fechaInicio
        if(!dto.fechaFin().isAfter(dto.fechaInicio())){
            throw new BusinessException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        // 2. Validar existencia y estado del Cliente
        Cliente cliente = clienteRepository.findById(dto.idCliente())
                .orElseThrow(()-> new ResourceNotFoundException("Cliente no encontrado con ID: " + dto.idCliente()));
        if (!cliente.getActivo()){
            throw new BusinessException("El cliente seleccionado se encuentra inactivo y no puede realizar alquileres");
        }

        // 3. Validar existencia y estado del vehículo
        Vehiculo vehiculo = vehiculoRepository.findById(dto.idVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + dto.idVehiculo()));

        // 3.1 Validar estado del vehículo
        // Lombok genera .getActivo() para tipos Boolean wrapper
        if (!vehiculo.getActivo()){
            throw new BusinessException("El vehículo seleccionado se encuentra inactivo");
        }

        // 4. Validar disponibilidad del vehículo (Solapamiento de períodos)
        boolean solapo = reservaRepository.existeSolapamiento(
                vehiculo.getIdVehiculo(),
                dto.fechaInicio(),
                dto.fechaFin(),
                EstadoReserva.CONFIRMADA
        );

        if (solapo){
            throw new BusinessException("El vehículo ya posee una reserva confirmada dentro del periodo solicitado");
        }

        //5. Algoritmo de cálculo de días e importe Total
        long horas = Duration.between(dto.fechaInicio(), dto.fechaFin()).toHours();
        long dias = (long) Math.ceil((double) horas / 24);
        if (dias == 0){
            dias =1; // Mínimo 1 día de alquiler
        }
        BigDecimal importeTotal = vehiculo.getPrecioDiario().multiply(BigDecimal.valueOf(dias));

        //6. Instancia y persistencia de la reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setVehiculo(vehiculo);
        reserva.setFechaInicio(dto.fechaInicio());
        reserva.setFechaFin(dto.fechaFin());
        reserva.setPrecioDiario(vehiculo.getPrecioDiario());
        reserva.setImporteTotal(importeTotal);
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        Reserva guardada = reservaRepository.save(reserva);

        // 7. Actualizar el estado del vehículo a RESERVADO para reflejar que está ocupado
        vehiculo.setEstado(EstadoVehiculo.RESERVADO);
        vehiculoRepository.save(vehiculo);

        return reservaMapper.mapToResponseDTO(guardada);

    }

    /**
     * Cancela una reserva existente mediante baja lógica (cambio de estado).
     * Valida que:
     * 1. La reserva exista
     * 2. El período de alquiler no haya comenzado aún
     * 3. La reserva no esté previamente cancelada o finalizada
     * Al cancelar, revierte el estado del vehículo a DISPONIBLE.
     *
     * @param idReserva ID de la reserva a cancelar
     * @return DTO con la reserva actualizada en estado CANCELADA
     */
    @Override
    @Transactional
    public ReservaResponseDTO cancelarReserva(Long idReserva){

        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + idReserva));

        // Validar que el periodo de alquiler aún no haya comenzado
        if (LocalDateTime.now().isAfter(reserva.getFechaInicio()) || LocalDateTime.now().isEqual(reserva.getFechaInicio())){
            throw new BusinessException("No se puede cancelar una reserva cuyo período de alquiler ya transcurrió");
        }

        if (reserva.getEstado() == EstadoReserva.CANCELADA){
            throw new BusinessException("La reserva ya se encuentra cancelada");
        }

        if (reserva.getEstado() == EstadoReserva.FINALIZADA){
            throw new BusinessException("No se puede cancelar una reserva que ya fue finalizada");
        }

        // Baja lógica de la reserva (modificación de estado)
        reserva.setEstado(EstadoReserva.CANCELADA);

        // Revertir el estado del vehículo a DISPONIBLE ya que la reserva se canceló
        Vehiculo vehiculo = reserva.getVehiculo();
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculoRepository.save(vehiculo);

        Reserva actualizada = reservaRepository.save(reserva);
        return reservaMapper.mapToResponseDTO(actualizada);
    }

    /**
     * Consulta una reserva por su ID y retorna sus detalles completos.
     * Operación de solo lectura (readOnly = true) optimizada para consulta.
     *
     * @param idReserva ID de la reserva a consultar
     * @return DTO con los datos completos de la reserva
     */
    @Override
    @Transactional(readOnly = true)
    public ReservaResponseDTO obtenerPorId(Long idReserva){
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + idReserva));
        return reservaMapper.mapToResponseDTO(reserva);
    }



}
