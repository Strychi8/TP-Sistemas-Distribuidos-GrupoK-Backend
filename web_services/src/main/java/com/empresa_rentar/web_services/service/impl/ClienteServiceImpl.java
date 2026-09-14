package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.response.ClienteResponseDTO;
import com.empresa_rentar.web_services.enums.NombreRol;
import com.empresa_rentar.web_services.exception.custom.DniAlreadyExistsException;
import com.empresa_rentar.web_services.mapper.ClienteMapper;
import com.empresa_rentar.web_services.model.Usuario;
import com.empresa_rentar.web_services.repository.IClienteRepository;
import com.empresa_rentar.web_services.service.IClienteService;
import com.empresa_rentar.web_services.service.IUsuarioRolService;
import com.empresa_rentar.web_services.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service("clienteService")
public class ClienteServiceImpl implements IClienteService {
    private final IClienteRepository clienteRepository;
    private final IUsuarioService usuarioService;
    private final IUsuarioRolService usuarioRolService;

    @Transactional
    @Override
    public ClienteResponseDTO crearCliente(ClienteRequestDTO request) {
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new DniAlreadyExistsException();
        }

        Usuario usuario = usuarioService.crearUsuario(request.getEmail(), request.getPassword());

        usuarioRolService.asignarRol(usuario, NombreRol.CLIENTE.toString());

        return ClienteMapper.toClienteResponseDTO(clienteRepository.save(ClienteMapper.toCliente(request, usuario)));
    }
}
