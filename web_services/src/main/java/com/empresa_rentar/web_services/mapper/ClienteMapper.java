package com.empresa_rentar.web_services.mapper;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.response.ClienteResponseDTO;
import com.empresa_rentar.web_services.model.Cliente;
import com.empresa_rentar.web_services.model.Usuario;

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
                .telefono(cliente.getDni())
                .fechaNacimiento(cliente.getFechaNacimiento())
                .build();
    }
}
