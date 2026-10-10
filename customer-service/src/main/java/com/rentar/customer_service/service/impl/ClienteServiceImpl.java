package com.rentar.customer_service.service.impl;

import com.rentar.customer_service.dto.request.ClienteRequestDTO;
import com.rentar.customer_service.dto.request.ClienteUpdateDTO;
import com.rentar.customer_service.dto.response.ClienteResponseDTO;
import com.rentar.customer_service.enums.NombreRol;
import com.rentar.customer_service.exception.custom.ClienteNotFoundException;
import com.rentar.customer_service.exception.custom.DniAlreadyExistsException;
import com.rentar.customer_service.exception.custom.EmailAlreadyExistsException;
import com.rentar.customer_service.mapper.ClienteMapper;
import com.rentar.customer_service.model.Cliente;
import com.rentar.customer_service.model.Usuario;
import com.rentar.customer_service.repository.IClienteRepository;
import com.rentar.customer_service.service.IClienteService;
import com.rentar.customer_service.service.IUsuarioRolService;
import com.rentar.customer_service.service.IUsuarioService;
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

    @Transactional
    @Override
    public ClienteResponseDTO obtenerMiPerfil(String email) {
        Cliente cliente = clienteRepository.findByEmail(email).orElseThrow(ClienteNotFoundException::new);
        return ClienteMapper.toClienteResponseDTO(cliente);
    }
}
