package com.rentar.customer_service.mapper;


import com.rentar.customer_service.dto.request.ClienteRequestDTO;
import com.rentar.customer_service.dto.response.ClienteResponseDTO;
import com.rentar.customer_service.model.Cliente;
import com.rentar.customer_service.model.Usuario;

public class ClienteMapper {
    public static Cliente toCliente(ClienteRequestDTO request, Usuario usuario){
        return Cliente.builder()
                .usuario(usuario)
                .dni(request.getDni())
                .email(request.getEmail())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .telefono(request.getTelefono())
                .fechaNacimiento(request.getFechaNacimiento())
                .build();
    }

    public static ClienteResponseDTO toClienteResponseDTO(Cliente cliente){
        return ClienteResponseDTO.builder()
                .idCliente(cliente.getIdCliente())
                .idUsuario(cliente.getUsuario().getIdUsuario())
                .email(cliente.getEmail())
                .dni(cliente.getDni())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .telefono(cliente.getTelefono())
                .fechaNacimiento(cliente.getFechaNacimiento())
                .activo(cliente.getActivo())
                .build();
    }
}
