package com.rentar.vehicle_service.service;

import com.rentar.vehicle_service.dto.request.VehiculoRequestDTO;
import com.rentar.vehicle_service.dto.request.VehiculoUpdateRequestDTO;
import com.rentar.vehicle_service.dto.response.VehiculoResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface IVehiculoService {

    VehiculoResponseDTO crearVehiculo(VehiculoRequestDTO dto);

    VehiculoResponseDTO actualizarVehiculo(Long id, VehiculoUpdateRequestDTO dto);

    VehiculoResponseDTO obtenerVehiculoPorId(Long id);

    VehiculoResponseDTO obtenerVehiculoPorPatente(String patente);

    List<VehiculoResponseDTO> listarVehiculos();

    List<VehiculoResponseDTO> listarVehiculosActivos();

    void eliminarVehiculo(Long id);
}

