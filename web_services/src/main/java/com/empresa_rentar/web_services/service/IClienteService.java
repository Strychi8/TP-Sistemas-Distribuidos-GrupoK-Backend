package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.response.ClienteResponseDTO;

public interface IClienteService {
    ClienteResponseDTO crearCliente(ClienteRequestDTO request);
    ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO request);
}
