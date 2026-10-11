package com.rentar.customer_service.service.impl;

import com.rentar.customer_service.dto.request.ClienteRequestDTO;
import com.rentar.customer_service.dto.request.ClienteUpdateDTO;
import com.rentar.customer_service.dto.response.ClienteResponseDTO;
import com.rentar.customer_service.enums.NombreRol;
import com.rentar.customer_service.exception.custom.BadRequestException;
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
import com.rentar.customer_service.validation.MayorDeEdadValidator;
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

    // Stateless y sin dependencias (igual que cuando lo instancia el framework de Bean
    // Validation sobre los DTOs de Hito 1): se reutiliza tal cual, sin reescribir la regla.
    private final MayorDeEdadValidator mayorDeEdadValidator = new MayorDeEdadValidator();

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
        Cliente cliente = buscarClientePorId(id);

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
        Cliente cliente = buscarClientePorId(id);

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
        return ClienteMapper.toClienteResponseDTO(buscarClientePorId(id));
    }

    @Transactional
    @Override
    public ClienteResponseDTO obtenerMiPerfil(String email) {
        Cliente cliente = clienteRepository.findByEmail(email).orElseThrow(ClienteNotFoundException::new);
        return ClienteMapper.toClienteResponseDTO(cliente);
    }

    /**
     * Verifica que el cliente exista por DNI. Usada por el RPC gRPC
     * VerificarClienteExistente (H2-4): "no existe" se traduce a
     * ClienteNotFoundException (NOT_FOUND vía GrpcGlobalExceptionHandler).
     */
    @Transactional
    @Override
    public ClienteResponseDTO verificarClienteExistente(String dni) {
        return ClienteMapper.toClienteResponseDTO(buscarClientePorDni(dni));
    }

    /**
     * Verifica que el cliente exista, esté activo y sea mayor de edad.
     * - "No existe" se distingue de "existe pero no cumple la regla de negocio" para que
     *   el Gateway (y luego el adapter gRPC) puedan mapear a NOT_FOUND vs INVALID_ARGUMENT.
     * - La validación de mayoría de edad reutiliza MayorDeEdadValidator (sin reescribir la
     *   regla) y conserva el mensaje exacto del Hito 1.
     */
    @Transactional
    @Override
    public ClienteResponseDTO verificarClienteActivo(Long id) {
        Cliente cliente = buscarClientePorId(id);

        if (Boolean.FALSE.equals(cliente.getActivo())) {
            throw new BadRequestException("El cliente se encuentra inactivo");
        }

        if (!mayorDeEdadValidator.isValid(cliente.getFechaNacimiento(), null)) {
            throw new BadRequestException("El cliente debe ser mayor de 18 años");
        }

        return ClienteMapper.toClienteResponseDTO(cliente);
    }

    /**
     * Único punto de búsqueda por id: centraliza el NotFound para que todos los
     * métodos de esta clase (CRUD y verificaciones) lancen la misma excepción.
     */
    private Cliente buscarClientePorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new);
    }

    /**
     * Único punto de búsqueda por DNI: misma excepción que buscarClientePorId,
     * para que VerificarClienteExistente mapee "no existe" a NOT_FOUND.
     */
    private Cliente buscarClientePorDni(String dni) {
        return clienteRepository.findByDni(dni).orElseThrow(ClienteNotFoundException::new);
    }
}
