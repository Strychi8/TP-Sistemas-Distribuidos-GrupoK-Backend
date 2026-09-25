package com.empresa_rentar.web_services.integration;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.request.ClienteUpdateDTO;
import com.empresa_rentar.web_services.model.Cliente;
import com.empresa_rentar.web_services.model.Usuario;
import com.empresa_rentar.web_services.repository.IClienteRepository;
import com.empresa_rentar.web_services.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integracion de clientes y usuarios contra el MySQL real (perfil "test").
 * Requiere la base levantada con docker compose.
 * Cada test corre en una transaccion que se revierte al finalizar, por lo que no
 * quedan datos de prueba persistidos.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@WithMockUser(authorities = "ADMINISTRADOR")
@DisplayName("Integracion - Clientes y Usuarios")
class ClienteIntegrationTest {

    private static final String EMAIL_SEED = "juan.perez@gmail.com";
    private static final String DNI_SEED = "40111222";

    private static final String EMAIL_NUEVO = "cliente.integracion@test.com";
    private static final String DNI_NUEVO = "39000111";
    private static final String PASSWORD_NUEVO = "secreto123";

    private static final Long ID_ROL_CLIENTE = 2L;
    private static final Long ID_INEXISTENTE = 999999L;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("GET /api/clientes: devuelve el listado con los clientes del seed")
    void deberiaListarTodosLosClientes() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].email", hasItem(EMAIL_SEED)))
                .andExpect(jsonPath("$[*].dni", hasItem(DNI_SEED)));
    }

    @Test
    @DisplayName("GET /api/clientes/activos: devuelve los clientes activos e incluye el seed")
    void deberiaListarSoloClientesActivos() throws Exception {
        mockMvc.perform(get("/api/clientes/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].email", hasItem(EMAIL_SEED)));
    }

    @Test
    @DisplayName("GET /api/clientes/{id}: devuelve el cliente con sus datos y el idUsuario")
    void deberiaObtenerClientePorId() throws Exception {
        Cliente seed = obtenerClienteSeed();

        mockMvc.perform(get("/api/clientes/{id}", seed.getIdCliente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(seed.getIdCliente()))
                .andExpect(jsonPath("$.idUsuario").value(seed.getUsuario().getIdUsuario()))
                .andExpect(jsonPath("$.email").value(EMAIL_SEED))
                .andExpect(jsonPath("$.dni").value(DNI_SEED))
                .andExpect(jsonPath("$.nombre").value(seed.getNombre()));
    }

    @Test
    @DisplayName("GET /api/clientes/{id}: responde 404 cuando el cliente no existe")
    void deberiaRetornar404CuandoElClienteNoExiste() throws Exception {
        mockMvc.perform(get("/api/clientes/{id}", ID_INEXISTENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"))
                .andExpect(jsonPath("$.path").value("/api/clientes/" + ID_INEXISTENTE));
    }

    @Test
    @DisplayName("POST /api/clientes: crea el cliente, su usuario (con password BCrypt) y el rol CLIENTE")
    void deberiaCrearClienteConSuUsuarioYRolCliente() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest(EMAIL_NUEVO, PASSWORD_NUEVO, DNI_NUEVO,
                                "Cliente", "Integracion", "1122334455", LocalDate.of(1995, 3, 15))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(jsonPath("$.idCliente").isNumber())
                .andExpect(jsonPath("$.idUsuario").isNumber())
                .andExpect(jsonPath("$.email").value(EMAIL_NUEVO))
                .andExpect(jsonPath("$.dni").value(DNI_NUEVO));

        // Verificacion directa en base de datos
        Usuario usuarioCreado = usuarioRepository.findAll().stream()
                .filter(u -> EMAIL_NUEVO.equals(u.getEmail()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se creo el usuario con email " + EMAIL_NUEVO));

        assertThat(usuarioCreado.getActivo()).isTrue();
        assertThat(usuarioCreado.getPassword()).isNotEqualTo(PASSWORD_NUEVO);
        assertThat(passwordEncoder.matches(PASSWORD_NUEVO, usuarioCreado.getPassword())).isTrue();

        Cliente clienteCreado = clienteRepository.findAll().stream()
                .filter(c -> DNI_NUEVO.equals(c.getDni()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se creo el cliente con DNI " + DNI_NUEVO));

        assertThat(clienteCreado.getActivo()).isTrue();
        assertThat(clienteCreado.getUsuario().getIdUsuario()).isEqualTo(usuarioCreado.getIdUsuario());
        assertThat(contarFilasUsuarioRol(usuarioCreado.getIdUsuario(), ID_ROL_CLIENTE)).isEqualTo(1L);
    }

    @Test
    @DisplayName("POST /api/clientes: responde 409 cuando el DNI ya esta registrado")
    void deberiaRetornar409CuandoElDniYaExiste() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest("otro.email@test.com", PASSWORD_NUEVO, DNI_SEED,
                                "Otro", "Cliente", "1122334455", LocalDate.of(1990, 1, 1))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("El DNI ya se encuentra registrado"));
    }

    @Test
    @DisplayName("POST /api/clientes: responde 409 cuando el email ya esta registrado")
    void deberiaRetornar409CuandoElEmailYaExiste() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest(EMAIL_SEED, PASSWORD_NUEVO, "39999888",
                                "Otro", "Cliente", "1122334455", LocalDate.of(1990, 1, 1))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("El email ya está asignado a otro usuario"));
    }

    @Test
    @DisplayName("POST /api/clientes: responde 400 con los errores de validacion")
    void deberiaRetornar400CuandoElPayloadEsInvalido() throws Exception {
        String payloadInvalido = """
                {
                  "email": "no-es-un-email",
                  "password": "",
                  "dni": "",
                  "nombre": "",
                  "apellido": "",
                  "telefono": "",
                  "fechaNacimiento": "2015-01-01"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(APPLICATION_JSON)
                        .content(payloadInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Se encontraron errores de validación en la petición"))
                .andExpect(jsonPath("$.validationErrors", hasItems(
                        "El DNI es obligatorio",
                        "El nombre es obligatorio",
                        "El cliente debe ser mayor de 18 años")));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: actualiza el cliente, el email de su usuario y reencripta la password")
    void deberiaActualizarClienteYUsuario() throws Exception {
        Cliente seed = obtenerClienteSeed();
        String emailActualizado = "juan.perez.actualizado@test.com";

        mockMvc.perform(put("/api/clientes/{id}", seed.getIdCliente())
                        .contentType(APPLICATION_JSON)
                        .content(jsonUpdateRequest(emailActualizado, PASSWORD_NUEVO, "39000222",
                                "Juan", "Perez Actualizado", "1199999999", LocalDate.of(1995, 3, 15))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(seed.getIdCliente()))
                .andExpect(jsonPath("$.email").value(emailActualizado))
                .andExpect(jsonPath("$.dni").value("39000222"))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.apellido").value("Perez Actualizado"));

        Cliente actualizado = clienteRepository.findById(seed.getIdCliente()).orElseThrow();
        assertThat(actualizado.getEmail()).isEqualTo(emailActualizado);
        assertThat(actualizado.getDni()).isEqualTo("39000222");
        assertThat(actualizado.getUsuario().getEmail()).isEqualTo(emailActualizado);
        assertThat(passwordEncoder.matches(PASSWORD_NUEVO, actualizado.getUsuario().getPassword())).isTrue();
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: sin password conserva la password existente (campo opcional)")
    void deberiaActualizarClienteSinPasswordConservandoLaExistente() throws Exception {
        Cliente seed = obtenerClienteSeed();
        String passwordAntes = seed.getUsuario().getPassword();
        String emailActualizado = "juan.sin.password@test.com";

        mockMvc.perform(put("/api/clientes/{id}", seed.getIdCliente())
                        .contentType(APPLICATION_JSON)
                        .content(jsonUpdateRequest(emailActualizado, null, "39000999",
                                "Juan", "Perez", "1123456789", LocalDate.of(1995, 3, 15))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(emailActualizado))
                .andExpect(jsonPath("$.dni").value("39000999"));

        Cliente actualizado = clienteRepository.findById(seed.getIdCliente()).orElseThrow();
        assertThat(actualizado.getEmail()).isEqualTo(emailActualizado);
        assertThat(actualizado.getUsuario().getEmail()).isEqualTo(emailActualizado);
        assertThat(actualizado.getUsuario().getPassword()).isEqualTo(passwordAntes);
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: responde 404 cuando el cliente no existe")
    void deberiaRetornar404CuandoActualizaClienteInexistente() throws Exception {
        mockMvc.perform(put("/api/clientes/{id}", ID_INEXISTENTE)
                        .contentType(APPLICATION_JSON)
                        .content(jsonUpdateRequest("nuevo.email@test.com", PASSWORD_NUEVO, "39000333",
                                "Nombre", "Apellido", "1122334455", LocalDate.of(1990, 1, 1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: responde 409 cuando el DNI pertenece a otro cliente")
    void deberiaRetornar409CuandoActualizaConDniDeOtroCliente() throws Exception {
        Cliente seed = obtenerClienteSeed();
        Cliente otro = obtenerOtroCliente(seed.getIdCliente());

        mockMvc.perform(put("/api/clientes/{id}", seed.getIdCliente())
                        .contentType(APPLICATION_JSON)
                        .content(jsonUpdateRequest("email.valido@test.com", PASSWORD_NUEVO, otro.getDni(),
                                "Nombre", "Apellido", "1122334455", LocalDate.of(1990, 1, 1))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("El DNI ya se encuentra registrado"));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: responde 409 cuando el email pertenece a otro cliente")
    void deberiaRetornar409CuandoActualizaConEmailDeOtroCliente() throws Exception {
        Cliente seed = obtenerClienteSeed();
        Cliente otro = obtenerOtroCliente(seed.getIdCliente());

        mockMvc.perform(put("/api/clientes/{id}", seed.getIdCliente())
                        .contentType(APPLICATION_JSON)
                        .content(jsonUpdateRequest(otro.getEmail(), PASSWORD_NUEVO, "39999333",
                                "Nombre", "Apellido", "1122334455", LocalDate.of(1990, 1, 1))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("El email ya está asignado a otro usuario"));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: responde 400 con los errores de validacion y password opcional")
    void deberiaRetornar400CuandoActualizaConPayloadInvalido() throws Exception {
        String payloadInvalido = """
                {
                  "email": "no-es-un-email",
                  "dni": "",
                  "nombre": "",
                  "apellido": "",
                  "telefono": "",
                  "fechaNacimiento": "2015-01-01"
                }
                """;

        mockMvc.perform(put("/api/clientes/{id}", ID_INEXISTENTE)
                        .contentType(APPLICATION_JSON)
                        .content(payloadInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Se encontraron errores de validación en la petición"))
                .andExpect(jsonPath("$.validationErrors", hasItems(
                        "El DNI es obligatorio",
                        "El nombre es obligatorio",
                        "El email debe ser válido",
                        "El cliente debe ser mayor de 18 años")))
                .andExpect(jsonPath("$.validationErrors", not(hasItem("La contraseña es obligatoria"))));
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id}: hace baja logica del cliente y de su usuario")
    void deberiaEliminarClienteLogicamente() throws Exception {
        Cliente seed = obtenerClienteSeed();
        Long idCliente = seed.getIdCliente();
        Long idUsuario = seed.getUsuario().getIdUsuario();

        mockMvc.perform(delete("/api/clientes/{id}", idCliente))
                .andExpect(status().isNoContent());

        Cliente eliminado = clienteRepository.findById(idCliente).orElseThrow();
        assertThat(eliminado.getActivo()).isFalse();

        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow();
        assertThat(usuario.getActivo()).isFalse();

        // El registro sigue existiendo (baja logica) pero ya no aparece entre los activos
        mockMvc.perform(get("/api/clientes/{id}", idCliente))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/clientes/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].dni", not(hasItem(DNI_SEED))));
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id}: responde 404 cuando el cliente no existe")
    void deberiaRetornar404CuandoEliminaClienteInexistente() throws Exception {
        mockMvc.perform(delete("/api/clientes/{id}", ID_INEXISTENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    private Cliente obtenerClienteSeed() {
        return clienteRepository.findAll().stream()
                .filter(c -> DNI_SEED.equals(c.getDni()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontro el cliente seed con DNI " + DNI_SEED
                                + ". Verificar que data.sql se haya ejecutado."));
    }

    private Cliente obtenerOtroCliente(Long idExcluido) {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream()
                .filter(c -> !idExcluido.equals(c.getIdCliente()))
                .filter(c -> c.getDni() != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Se necesitan al menos dos clientes en la base para este test."));
    }

    private long contarFilasUsuarioRol(Long idUsuario, Long idRol) {
        Number cantidad = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM usuario_rol WHERE usuario_id = :idUsuario AND rol_id = :idRol")
                .setParameter("idUsuario", idUsuario)
                .setParameter("idRol", idRol)
                .getSingleResult();
        return cantidad.longValue();
    }

    /**
     * ClienteRequestDTO no expone setters ni constructor con argumentos,
     * por eso los campos se cargan por reflexion y luego se serializan a JSON.
     */
    private String jsonRequest(String email, String password, String dni, String nombre,
                               String apellido, String telefono, LocalDate fechaNacimiento) throws Exception {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        ReflectionTestUtils.setField(dto, "email", email);
        ReflectionTestUtils.setField(dto, "password", password);
        ReflectionTestUtils.setField(dto, "dni", dni);
        ReflectionTestUtils.setField(dto, "nombre", nombre);
        ReflectionTestUtils.setField(dto, "apellido", apellido);
        ReflectionTestUtils.setField(dto, "telefono", telefono);
        ReflectionTestUtils.setField(dto, "fechaNacimiento", fechaNacimiento);
        return objectMapper.writeValueAsString(dto);
    }

    /**
     * ClienteUpdateDTO no expone setters ni constructor con argumentos, por eso
     * los campos se cargan por reflexion. La password es opcional (puede ser null).
     */
    private String jsonUpdateRequest(String email, String password, String dni, String nombre,
                                     String apellido, String telefono, LocalDate fechaNacimiento) throws Exception {
        ClienteUpdateDTO dto = new ClienteUpdateDTO();
        ReflectionTestUtils.setField(dto, "email", email);
        ReflectionTestUtils.setField(dto, "password", password);
        ReflectionTestUtils.setField(dto, "dni", dni);
        ReflectionTestUtils.setField(dto, "nombre", nombre);
        ReflectionTestUtils.setField(dto, "apellido", apellido);
        ReflectionTestUtils.setField(dto, "telefono", telefono);
        ReflectionTestUtils.setField(dto, "fechaNacimiento", fechaNacimiento);
        return objectMapper.writeValueAsString(dto);
    }
}
