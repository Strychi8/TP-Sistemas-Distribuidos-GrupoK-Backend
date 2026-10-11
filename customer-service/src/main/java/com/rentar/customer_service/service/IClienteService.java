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

    /**
     * Verifica que el cliente exista por DNI. Consumido por el RPC gRPC
     * VerificarClienteExistente (H2-4): el Gateway lo usa para distinguir
     * "no existe" (NOT_FOUND) de otras reglas de negocio.
     */
    ClienteResponseDTO verificarClienteExistente(String dni);

    /**
     * Verifica que el cliente exista, esté activo y sea mayor de edad (MayorDeEdadValidator).
     * Pensado para el futuro RPC gRPC VerificarClienteActivo (H2-4).
     */
    ClienteResponseDTO verificarClienteActivo(Long id);
}
