package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.dto.request.VehiculoDisponibilidadRequestDTO;
import com.empresa_rentar.web_services.dto.request.VehiculoRequestDTO;
import com.empresa_rentar.web_services.dto.request.VehiculoUpdateRequestDTO;
import com.empresa_rentar.web_services.dto.response.VehiculoResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface VehiculoService {

    VehiculoResponseDTO crearVehiculo(VehiculoRequestDTO dto);

    VehiculoResponseDTO actualizarVehiculo(Long id, VehiculoUpdateRequestDTO dto);

    VehiculoResponseDTO obtenerVehiculoPorId(Long id);

    VehiculoResponseDTO obtenerVehiculoPorPatente(String patente);

    List<VehiculoResponseDTO> listarVehiculos();

    List<VehiculoResponseDTO> listarVehiculosActivos();

    void eliminarVehiculo(Long id);

    List<VehiculoResponseDTO> consultarDisponibilidad(LocalDateTime inicio, LocalDateTime fin, VehiculoDisponibilidadRequestDTO filtro);
}
