package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.request.ClienteUpdateDTO;
import com.empresa_rentar.web_services.dto.response.ClienteResponseDTO;
import com.empresa_rentar.web_services.enums.NombreRol;
import com.empresa_rentar.web_services.exception.custom.ClienteNotFoundException;
import com.empresa_rentar.web_services.exception.custom.DniAlreadyExistsException;
import com.empresa_rentar.web_services.exception.custom.EmailAlreadyExistsException;
import com.empresa_rentar.web_services.model.Cliente;
import com.empresa_rentar.web_services.model.Usuario;
import com.empresa_rentar.web_services.repository.IClienteRepository;
import com.empresa_rentar.web_services.service.IUsuarioRolService;
import com.empresa_rentar.web_services.service.IUsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteServiceImpl - tests unitarios")
class ClienteServiceImplTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_USUARIO = 10L;
    private static final String EMAIL = "nuevo.cliente@test.com";
    private static final String PASSWORD = "secreto123";
    private static final String DNI = "30111222";
    private static final String NOMBRE = "Nuevo";
    private static final String APELLIDO = "Cliente";
    private static final String TELEFONO = "1122334455";
    private static final LocalDate FECHA_NACIMIENTO = LocalDate.of(1995, 3, 15);

    @Mock
    private IClienteRepository clienteRepository;

    @Mock
    private IUsuarioService usuarioService;

    @Mock
    private IUsuarioRolService usuarioRolService;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteRequestDTO request;
    private ClienteUpdateDTO updateRequest;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        request = buildRequest(EMAIL, PASSWORD, DNI, NOMBRE, APELLIDO, TELEFONO, FECHA_NACIMIENTO);
        updateRequest = buildUpdateRequest(EMAIL, PASSWORD, DNI, NOMBRE, APELLIDO, TELEFONO, FECHA_NACIMIENTO);
        usuario = Usuario.builder()
                .idUsuario(ID_USUARIO)
                .email(EMAIL)
                .password("$2a$10$hashGuardado")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("crearCliente: lanza DniAlreadyExistsException cuando el DNI ya existe")
    void deberiaLanzarDniAlreadyExistsCuandoElDniYaExiste() {
        when(clienteRepository.existsByDni(DNI)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crearCliente(request))
                .isInstanceOf(DniAlreadyExistsException.class)
                .hasMessage("El DNI ya se encuentra registrado");

        verify(clienteRepository, never()).save(any(Cliente.class));
        verifyNoInteractions(usuarioService, usuarioRolService);
    }

    @Test
    @DisplayName("crearCliente: crea el usuario, asigna el rol CLIENTE y persiste el cliente")
    void deberiaCrearClienteCorrectamente() {
        when(clienteRepository.existsByDni(DNI)).thenReturn(false);
        when(usuarioService.crearUsuario(EMAIL, PASSWORD)).thenReturn(usuario);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente aGuardar = invocation.getArgument(0);
            aGuardar.setIdCliente(ID_CLIENTE);
            return aGuardar;
        });

        ClienteResponseDTO response = clienteService.crearCliente(request);

        assertThat(response.getIdCliente()).isEqualTo(ID_CLIENTE);
        assertThat(response.getIdUsuario()).isEqualTo(ID_USUARIO);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getDni()).isEqualTo(DNI);
        assertThat(response.getNombre()).isEqualTo(NOMBRE);
        assertThat(response.getApellido()).isEqualTo(APELLIDO);
        assertThat(response.getTelefono()).isEqualTo(TELEFONO);
        assertThat(response.getFechaNacimiento()).isEqualTo(FECHA_NACIMIENTO);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        Cliente persistido = captor.getValue();
        assertThat(persistido.getUsuario()).isEqualTo(usuario);
        assertThat(persistido.getDni()).isEqualTo(DNI);
        assertThat(persistido.getEmail()).isEqualTo(EMAIL);
        assertThat(persistido.getActivo()).isTrue();

        verify(usuarioService).crearUsuario(EMAIL, PASSWORD);
        verify(usuarioRolService).asignarRol(usuario, NombreRol.CLIENTE);
    }

    @Test
    @DisplayName("actualizarCliente: lanza ClienteNotFoundException cuando el id no existe")
    void deberiaLanzarClienteNotFoundCuandoNoExisteParaActualizar() {
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.actualizarCliente(ID_CLIENTE, updateRequest))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessage("Cliente no encontrado");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("actualizarCliente: lanza DniAlreadyExistsException cuando el DNI pertenece a otro cliente")
    void deberiaLanzarDniAlreadyExistsCuandoElDniPerteneceAOtroCliente() {
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(buildClientePersistido()));
        when(clienteRepository.existsByDniAndIdClienteNot(DNI, ID_CLIENTE)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.actualizarCliente(ID_CLIENTE, updateRequest))
                .isInstanceOf(DniAlreadyExistsException.class)
                .hasMessage("El DNI ya se encuentra registrado");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("actualizarCliente: lanza EmailAlreadyExistsException cuando el email pertenece a otro cliente")
    void deberiaLanzarEmailAlreadyExistsCuandoElEmailPerteneceAOtroCliente() {
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(buildClientePersistido()));
        when(clienteRepository.existsByDniAndIdClienteNot(anyString(), eq(ID_CLIENTE))).thenReturn(false);
        when(clienteRepository.existsByEmailAndIdClienteNot(anyString(), eq(ID_CLIENTE))).thenReturn(true);

        assertThatThrownBy(() -> clienteService.actualizarCliente(ID_CLIENTE, updateRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("El email ya está asignado a otro usuario");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("actualizarCliente: actualiza los datos del cliente y delega el email/password del usuario")
    void deberiaActualizarClienteCorrectamente() {
        Cliente existente = buildClientePersistido();
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByDniAndIdClienteNot(anyString(), eq(ID_CLIENTE))).thenReturn(false);
        when(clienteRepository.existsByEmailAndIdClienteNot(anyString(), eq(ID_CLIENTE))).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Simula el contrato de IUsuarioService.actualizarUsuario: muta el email del usuario recibido.
        doAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setEmail(invocation.getArgument(1));
            return null;
        }).when(usuarioService).actualizarUsuario(any(), anyString(), any());

        ClienteUpdateDTO cambios = buildUpdateRequest(
                "actualizado@test.com", "otraPassword", "39999888",
                "Nombre Actualizado", "Apellido Actualizado", "1100000000",
                LocalDate.of(1990, 1, 1));

        ClienteResponseDTO response = clienteService.actualizarCliente(ID_CLIENTE, cambios);

        assertThat(response.getEmail()).isEqualTo("actualizado@test.com");
        assertThat(response.getDni()).isEqualTo("39999888");
        assertThat(response.getNombre()).isEqualTo("Nombre Actualizado");
        assertThat(response.getApellido()).isEqualTo("Apellido Actualizado");
        assertThat(response.getTelefono()).isEqualTo("1100000000");
        assertThat(response.getFechaNacimiento()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(response.getIdUsuario()).isEqualTo(ID_USUARIO);

        assertThat(existente.getUsuario().getEmail()).isEqualTo("actualizado@test.com");
        verify(usuarioService).actualizarUsuario(existente.getUsuario(), "actualizado@test.com", "otraPassword");
        verify(clienteRepository).save(existente);
    }

    @Test
    @DisplayName("actualizarCliente: delega password null al usuario sin reencriptar")
    void deberiaActualizarClienteSinPasswordCuandoLlegaNull() {
        Cliente existente = buildClientePersistido();
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByDniAndIdClienteNot(anyString(), eq(ID_CLIENTE))).thenReturn(false);
        when(clienteRepository.existsByEmailAndIdClienteNot(anyString(), eq(ID_CLIENTE))).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteUpdateDTO cambios = buildUpdateRequest(
                "actualizado@test.com", null, "39999888",
                "Nombre Actualizado", "Apellido Actualizado", "1100000000",
                LocalDate.of(1990, 1, 1));

        clienteService.actualizarCliente(ID_CLIENTE, cambios);

        verify(usuarioService).actualizarUsuario(existente.getUsuario(), "actualizado@test.com", null);
        verify(clienteRepository).save(existente);
    }

    @Test
    @DisplayName("actualizarCliente: delega password en blanco al usuario sin reencriptar")
    void deberiaActualizarClienteSinPasswordCuandoLlegaEnBlanco() {
        Cliente existente = buildClientePersistido();
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByDniAndIdClienteNot(anyString(), eq(ID_CLIENTE))).thenReturn(false);
        when(clienteRepository.existsByEmailAndIdClienteNot(anyString(), eq(ID_CLIENTE))).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteUpdateDTO cambios = buildUpdateRequest(
                "actualizado@test.com", "   ", "39999888",
                "Nombre Actualizado", "Apellido Actualizado", "1100000000",
                LocalDate.of(1990, 1, 1));

        clienteService.actualizarCliente(ID_CLIENTE, cambios);

        verify(usuarioService).actualizarUsuario(existente.getUsuario(), "actualizado@test.com", "   ");
        verify(clienteRepository).save(existente);
    }

    @Test
    @DisplayName("eliminarCliente: lanza ClienteNotFoundException cuando el id no existe")
    void deberiaLanzarClienteNotFoundCuandoNoExisteParaEliminar() {
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.eliminarCliente(ID_CLIENTE))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessage("Cliente no encontrado");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("eliminarCliente: hace baja logica del cliente y de su usuario")
    void deberiaEliminarClienteLogicamente() {
        Cliente existente = buildClientePersistido();
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(existente));

        clienteService.eliminarCliente(ID_CLIENTE);

        assertThat(existente.getActivo()).isFalse();
        assertThat(existente.getUsuario().getActivo()).isFalse();
        verify(clienteRepository).save(existente);
    }

    @Test
    @DisplayName("eliminarCliente: no falla cuando el cliente no tiene usuario asociado")
    void deberiaEliminarClienteSinUsuarioSinLanzarExcepcion() {
        Cliente sinUsuario = Cliente.builder()
                .idCliente(2L)
                .dni("12345678")
                .nombre("Sin")
                .apellido("Usuario")
                .email("sin.usuario@test.com")
                .build();
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(sinUsuario));

        clienteService.eliminarCliente(2L);

        assertThat(sinUsuario.getActivo()).isFalse();
        assertThat(sinUsuario.getUsuario()).isNull();
        verify(clienteRepository).save(sinUsuario);
    }

    @Test
    @DisplayName("listarClientes: mapea todos los clientes incluyendo el idUsuario")
    void deberiaListarTodosLosClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of(buildClientePersistido()));

        List<ClienteResponseDTO> resultado = clienteService.listarClientes();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdCliente()).isEqualTo(ID_CLIENTE);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(ID_USUARIO);
        assertThat(resultado.get(0).getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.get(0).getDni()).isEqualTo(DNI);
    }

    @Test
    @DisplayName("listarClientesActivos: devuelve solo los clientes activos")
    void deberiaListarClientesActivos() {
        when(clienteRepository.findAllByActivoTrue()).thenReturn(List.of(buildClientePersistido()));

        List<ClienteResponseDTO> resultado = clienteService.listarClientesActivos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(ID_USUARIO);
        verify(clienteRepository).findAllByActivoTrue();
    }

    @Test
    @DisplayName("obtenerClientePorId: devuelve el cliente mapeado cuando existe")
    void deberiaObtenerClientePorId() {
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(buildClientePersistido()));

        ClienteResponseDTO response = clienteService.obtenerClientePorId(ID_CLIENTE);

        assertThat(response.getIdCliente()).isEqualTo(ID_CLIENTE);
        assertThat(response.getIdUsuario()).isEqualTo(ID_USUARIO);
        assertThat(response.getNombre()).isEqualTo(NOMBRE);
    }

    @Test
    @DisplayName("obtenerClientePorId: lanza ClienteNotFoundException cuando no existe")
    void deberiaLanzarClienteNotFoundCuandoObtienePorIdInexistente() {
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerClientePorId(ID_CLIENTE))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessage("Cliente no encontrado");
    }

    private Cliente buildClientePersistido() {
        return Cliente.builder()
                .idCliente(ID_CLIENTE)
                .usuario(usuario)
                .dni(DNI)
                .nombre(NOMBRE)
                .apellido(APELLIDO)
                .email(EMAIL)
                .telefono(TELEFONO)
                .fechaNacimiento(FECHA_NACIMIENTO)
                .activo(true)
                .build();
    }

    /**
     * Los DTOs de request no exponen setters ni constructor con argumentos,
     * por eso los campos se cargan por reflexion.
     */
    private ClienteRequestDTO buildRequest(String email, String password, String dni, String nombre,
                                           String apellido, String telefono, LocalDate fechaNacimiento) {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        ReflectionTestUtils.setField(dto, "email", email);
        ReflectionTestUtils.setField(dto, "password", password);
        ReflectionTestUtils.setField(dto, "dni", dni);
        ReflectionTestUtils.setField(dto, "nombre", nombre);
        ReflectionTestUtils.setField(dto, "apellido", apellido);
        ReflectionTestUtils.setField(dto, "telefono", telefono);
        ReflectionTestUtils.setField(dto, "fechaNacimiento", fechaNacimiento);
        return dto;
    }

    /**
     * ClienteUpdateDTO no expone setters ni constructor con argumentos, por eso
     * los campos se cargan por reflexion. La password es opcional (puede ser null).
     */
    private ClienteUpdateDTO buildUpdateRequest(String email, String password, String dni, String nombre,
                                                String apellido, String telefono, LocalDate fechaNacimiento) {
        ClienteUpdateDTO dto = new ClienteUpdateDTO();
        ReflectionTestUtils.setField(dto, "email", email);
        ReflectionTestUtils.setField(dto, "password", password);
        ReflectionTestUtils.setField(dto, "dni", dni);
        ReflectionTestUtils.setField(dto, "nombre", nombre);
        ReflectionTestUtils.setField(dto, "apellido", apellido);
        ReflectionTestUtils.setField(dto, "telefono", telefono);
        ReflectionTestUtils.setField(dto, "fechaNacimiento", fechaNacimiento);
        return dto;
    }
}
