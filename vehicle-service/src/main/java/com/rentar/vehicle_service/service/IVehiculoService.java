package com.rentar.vehicle_service.service;

import com.rentar.vehicle_service.dto.request.FiltroDisponibilidadDTO;
import com.rentar.vehicle_service.dto.request.VehiculoRequestDTO;
import com.rentar.vehicle_service.dto.request.VehiculoUpdateRequestDTO;
import com.rentar.vehicle_service.dto.response.VehiculoResponseDTO;
import com.rentar.vehicle_service.enums.EstadoVehiculo;

import java.time.LocalDateTime;
import java.util.List;

public interface IVehiculoService {

    VehiculoResponseDTO crearVehiculo(VehiculoRequestDTO dto);

    VehiculoResponseDTO actualizarVehiculo(Long id, VehiculoUpdateRequestDTO dto);

    VehiculoResponseDTO actualizarEstado(Long id, EstadoVehiculo nuevoEstado);

    void eliminarVehiculo(Long id);

    List<VehiculoResponseDTO> listarVehiculos();

    List<VehiculoResponseDTO> listarVehiculosActivos();

    VehiculoResponseDTO obtenerVehiculoPorId(Long id);

    VehiculoResponseDTO obtenerVehiculoPorPatente(String patente);

    List<VehiculoResponseDTO> consultarDisponibilidad(FiltroDisponibilidadDTO filtro);
}