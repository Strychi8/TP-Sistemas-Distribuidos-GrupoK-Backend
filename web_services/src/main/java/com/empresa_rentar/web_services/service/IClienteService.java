package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.request.ClienteUpdateDTO;
import com.empresa_rentar.web_services.dto.response.ClienteResponseDTO;

import java.util.List;

public interface IClienteService {
    ClienteResponseDTO crearCliente(ClienteRequestDTO request);
    ClienteResponseDTO actualizarCliente(Long id, ClienteUpdateDTO request);
    void eliminarCliente(Long id);
    List<ClienteResponseDTO> listarClientes();
    List<ClienteResponseDTO> listarClientesActivos();
    ClienteResponseDTO obtenerClientePorId(Long id);
}
