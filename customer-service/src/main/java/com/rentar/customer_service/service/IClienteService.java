package com.rentar.customer_service.service;


import com.rentar.customer_service.dto.request.ClienteRequestDTO;
import com.rentar.customer_service.dto.request.ClienteUpdateDTO;
import com.rentar.customer_service.dto.response.ClienteResponseDTO;

import java.util.List;

public interface IClienteService {
    ClienteResponseDTO crearCliente(ClienteRequestDTO request);
    ClienteResponseDTO actualizarCliente(Long id, ClienteUpdateDTO request);
    void eliminarCliente(Long id);
    List<ClienteResponseDTO> listarClientes();
    List<ClienteResponseDTO> listarClientesActivos();
    ClienteResponseDTO obtenerClientePorId(Long id);
    ClienteResponseDTO obtenerMiPerfil(String email);
}
