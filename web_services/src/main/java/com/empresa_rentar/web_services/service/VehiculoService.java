package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.dto.request.VehiculoRequestDTO;
import com.empresa_rentar.web_services.dto.request.VehiculoUpdateRequestDTO;
import com.empresa_rentar.web_services.dto.response.VehiculoResponseDTO;

import java.util.List;

public interface VehiculoService {

    VehiculoResponseDTO crearVehiculo(VehiculoRequestDTO dto);

    VehiculoResponseDTO actualizarVehiculo(Long id, VehiculoUpdateRequestDTO dto);

    VehiculoResponseDTO buscarPorId(Long id);

    VehiculoResponseDTO buscarPorPatente(String patente);

    List<VehiculoResponseDTO> listarTodos();

    List<VehiculoResponseDTO> listarActivos();

    void darDeBaja(Long id);
}
