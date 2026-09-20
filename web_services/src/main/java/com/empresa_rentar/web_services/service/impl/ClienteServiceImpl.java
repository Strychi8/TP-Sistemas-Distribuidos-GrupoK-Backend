package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.request.ClienteUpdateDTO;
import com.empresa_rentar.web_services.dto.response.ClienteResponseDTO;
import com.empresa_rentar.web_services.enums.NombreRol;
import com.empresa_rentar.web_services.exception.custom.ClienteNotFoundException;
import com.empresa_rentar.web_services.exception.custom.DniAlreadyExistsException;
import com.empresa_rentar.web_services.exception.custom.EmailAlreadyExistsException;
import com.empresa_rentar.web_services.mapper.ClienteMapper;
import com.empresa_rentar.web_services.model.Cliente;
import com.empresa_rentar.web_services.model.Usuario;
import com.empresa_rentar.web_services.repository.IClienteRepository;
import com.empresa_rentar.web_services.service.IClienteService;
import com.empresa_rentar.web_services.service.IUsuarioRolService;
import com.empresa_rentar.web_services.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service("clienteService")
public class ClienteServiceImpl implements IClienteService {
    private final IClienteRepository clienteRepository;
    private final IUsuarioService usuarioService;
    private final IUsuarioRolService usuarioRolService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public ClienteResponseDTO crearCliente(ClienteRequestDTO request) {
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new DniAlreadyExistsException();
        }

        Usuario usuario = usuarioService.crearUsuario(request.getEmail(), request.getPassword());

        usuarioRolService.asignarRol(usuario, NombreRol.CLIENTE);

        return ClienteMapper.toClienteResponseDTO(clienteRepository.save(ClienteMapper.toCliente(request, usuario)));
    }

    @Transactional
    @Override
    public ClienteResponseDTO actualizarCliente(Long id, ClienteUpdateDTO request) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new);

        if (clienteRepository.existsByDniAndIdClienteNot(request.getDni(), id)) {
            throw new DniAlreadyExistsException();
        }
        if (clienteRepository.existsByEmailAndIdClienteNot(request.getEmail(), id)) {
            throw new EmailAlreadyExistsException();
        }

        cliente.setDni(request.getDni());
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setFechaNacimiento(request.getFechaNacimiento());

        usuarioService.actualizarUsuario(
                cliente.getUsuario(),
                request.getEmail(),
                request.getPassword()
        );

        return ClienteMapper.toClienteResponseDTO(clienteRepository.save(cliente));
    }

    @Transactional
    @Override
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new);

        cliente.setActivo(false);

        if (cliente.getUsuario() != null) {
            cliente.getUsuario().setActivo(false);
        }

        clienteRepository.save(cliente);
    }

    @Transactional
    @Override
    public List<ClienteResponseDTO> listarClientes() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteMapper::toClienteResponseDTO)
                .toList();
    }

    @Transactional
    @Override
    public List<ClienteResponseDTO> listarClientesActivos(){
        return clienteRepository.findAllByActivoTrue()
                .stream()
                .map(ClienteMapper::toClienteResponseDTO)
                .toList();
    }

    @Transactional
    @Override
    public ClienteResponseDTO obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new);

        return ClienteMapper.toClienteResponseDTO(cliente);
    }
}
