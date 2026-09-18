package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.request.ClienteUpdateDTO;
import com.empresa_rentar.web_services.dto.response.ClienteResponseDTO;
import com.empresa_rentar.web_services.exception.custom.ClienteNotFoundException;
import com.empresa_rentar.web_services.exception.custom.DniAlreadyExistsException;
import com.empresa_rentar.web_services.exception.custom.EmailAlreadyExistsException;
import com.empresa_rentar.web_services.service.IClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@DisplayName("ClienteController - tests unitarios de la capa web")
class ClienteControllerTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_USUARIO = 10L;
    private static final String EMAIL = "cliente@test.com";
    private static final String DNI = "30111222";
    private static final LocalDate FECHA_NACIMIENTO = LocalDate.of(1995, 3, 15);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IClienteService clienteService;

    @Test
    @DisplayName("POST /api/clientes: crea el cliente y responde 201 con Location y body")
    void deberiaCrearClienteYResponder201() throws Exception {
        when(clienteService.crearCliente(any(ClienteRequestDTO.class))).thenReturn(buildResponse());

        mockMvc.perform(post("/api/clientes")
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequestValido()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/clientes/1"))
                .andExpect(jsonPath("$.idCliente").value(ID_CLIENTE))
                .andExpect(jsonPath("$.idUsuario").value(ID_USUARIO))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.dni").value(DNI))
                .andExpect(jsonPath("$.nombre").value("Nombre"))
                .andExpect(jsonPath("$.apellido").value("Apellido"))
                .andExpect(jsonPath("$.telefono").value("1122334455"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1995-03-15"));
    }

    @Test
    @DisplayName("POST /api/clientes: responde 400 con los mensajes de validacion cuando el payload es invalido")
    void deberiaResponder400CuandoElPayloadEsInvalido() throws Exception {
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
                .andExpect(jsonPath("$.path").value("/api/clientes"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors", hasItems(
                        "El DNI es obligatorio",
                        "El email debe ser válido",
                        "La contraseña es obligatoria",
                        "El cliente debe ser mayor de 18 años")));

        verify(clienteService, never()).crearCliente(any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/clientes: responde 409 cuando el DNI ya esta registrado")
    void deberiaResponder409CuandoElDniYaExiste() throws Exception {
        when(clienteService.crearCliente(any(ClienteRequestDTO.class)))
                .thenThrow(new DniAlreadyExistsException());

        mockMvc.perform(post("/api/clientes")
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequestValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("El DNI ya se encuentra registrado"));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: responde 200 con el cliente actualizado")
    void deberiaActualizarClienteYResponder200() throws Exception {
        when(clienteService.actualizarCliente(eq(ID_CLIENTE), any(ClienteUpdateDTO.class)))
                .thenReturn(buildResponse());

        mockMvc.perform(put("/api/clientes/{id}", ID_CLIENTE)
                        .contentType(APPLICATION_JSON)
                        .content(jsonUpdateValido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(ID_CLIENTE))
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: acepta un payload sin password porque es opcional en la actualizacion")
    void deberiaActualizarClienteSinPasswordYResponder200() throws Exception {
        when(clienteService.actualizarCliente(eq(ID_CLIENTE), any(ClienteUpdateDTO.class)))
                .thenReturn(buildResponse());

        String payloadSinPassword = """
                {
                  "email": "cliente@test.com",
                  "dni": "30111222",
                  "nombre": "Nombre",
                  "apellido": "Apellido",
                  "telefono": "1122334455",
                  "fechaNacimiento": "1995-03-15"
                }
                """;

        mockMvc.perform(put("/api/clientes/{id}", ID_CLIENTE)
                        .contentType(APPLICATION_JSON)
                        .content(payloadSinPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(ID_CLIENTE))
                .andExpect(jsonPath("$.email").value(EMAIL));

        verify(clienteService).actualizarCliente(eq(ID_CLIENTE), any(ClienteUpdateDTO.class));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: responde 400 con los mensajes de validacion cuando el payload es invalido")
    void deberiaResponder400CuandoElUpdatePayloadEsInvalido() throws Exception {
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

        mockMvc.perform(put("/api/clientes/{id}", ID_CLIENTE)
                        .contentType(APPLICATION_JSON)
                        .content(payloadInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.path").value("/api/clientes/1"))
                .andExpect(jsonPath("$.validationErrors", hasItems(
                        "El DNI es obligatorio",
                        "El email debe ser válido",
                        "El cliente debe ser mayor de 18 años")))
                .andExpect(jsonPath("$.validationErrors", not(hasItem("La contraseña es obligatoria"))));

        verify(clienteService, never()).actualizarCliente(anyLong(), any(ClienteUpdateDTO.class));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: responde 409 cuando el DNI del update ya esta registrado")
    void deberiaResponder409CuandoElDniDelUpdateYaExiste() throws Exception {
        when(clienteService.actualizarCliente(eq(ID_CLIENTE), any(ClienteUpdateDTO.class)))
                .thenThrow(new DniAlreadyExistsException());

        mockMvc.perform(put("/api/clientes/{id}", ID_CLIENTE)
                        .contentType(APPLICATION_JSON)
                        .content(jsonUpdateValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("El DNI ya se encuentra registrado"));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: responde 409 cuando el email del update ya esta registrado")
    void deberiaResponder409CuandoElEmailDelUpdateYaExiste() throws Exception {
        when(clienteService.actualizarCliente(eq(ID_CLIENTE), any(ClienteUpdateDTO.class)))
                .thenThrow(new EmailAlreadyExistsException());

        mockMvc.perform(put("/api/clientes/{id}", ID_CLIENTE)
                        .contentType(APPLICATION_JSON)
                        .content(jsonUpdateValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("El email ya está asignado a otro usuario"));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id}: responde 404 cuando el cliente no existe")
    void deberiaResponder404CuandoElClienteAActualizarNoExiste() throws Exception {
        when(clienteService.actualizarCliente(eq(99L), any(ClienteUpdateDTO.class)))
                .thenThrow(new ClienteNotFoundException());

        mockMvc.perform(put("/api/clientes/{id}", 99L)
                        .contentType(APPLICATION_JSON)
                        .content(jsonUpdateValido()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id}: responde 204 sin body")
    void deberiaEliminarClienteYResponder204() throws Exception {
        mockMvc.perform(delete("/api/clientes/{id}", ID_CLIENTE))
                .andExpect(status().isNoContent());

        verify(clienteService).eliminarCliente(ID_CLIENTE);
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id}: responde 404 cuando el cliente no existe")
    void deberiaResponder404CuandoElClienteAEliminarNoExiste() throws Exception {
        org.mockito.Mockito.doThrow(new ClienteNotFoundException())
                .when(clienteService).eliminarCliente(99L);

        mockMvc.perform(delete("/api/clientes/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    @Test
    @DisplayName("GET /api/clientes: responde 200 con el listado completo")
    void deberiaListarTodosLosClientes() throws Exception {
        when(clienteService.listarClientes()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].idCliente").value(ID_CLIENTE))
                .andExpect(jsonPath("$[0].idUsuario").value(ID_USUARIO))
                .andExpect(jsonPath("$[0].email").value(EMAIL));
    }

    @Test
    @DisplayName("GET /api/clientes/activos: responde 200 con los clientes activos y no cae en /{id}")
    void deberiaListarClientesActivos() throws Exception {
        when(clienteService.listarClientesActivos()).thenReturn(List.of(buildResponse()));
        when(clienteService.obtenerClientePorId(anyLong())).thenThrow(new ClienteNotFoundException());

        mockMvc.perform(get("/api/clientes/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(EMAIL));

        verify(clienteService).listarClientesActivos();
        verify(clienteService, never()).obtenerClientePorId(anyLong());
    }

    @Test
    @DisplayName("GET /api/clientes/{id}: responde 200 con el cliente encontrado")
    void deberiaObtenerClientePorId() throws Exception {
        when(clienteService.obtenerClientePorId(ID_CLIENTE)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/clientes/{id}", ID_CLIENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(ID_CLIENTE))
                .andExpect(jsonPath("$.dni").value(DNI))
                .andExpect(jsonPath("$.fechaNacimiento").value("1995-03-15"));
    }

    @Test
    @DisplayName("GET /api/clientes/{id}: responde 404 cuando el cliente no existe")
    void deberiaResponder404CuandoElClienteNoExiste() throws Exception {
        when(clienteService.obtenerClientePorId(99L)).thenThrow(new ClienteNotFoundException());

        mockMvc.perform(get("/api/clientes/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"))
                .andExpect(jsonPath("$.path").value("/api/clientes/99"));
    }

    private ClienteResponseDTO buildResponse() {
        return ClienteResponseDTO.builder()
                .idCliente(ID_CLIENTE)
                .idUsuario(ID_USUARIO)
                .email(EMAIL)
                .dni(DNI)
                .nombre("Nombre")
                .apellido("Apellido")
                .telefono("1122334455")
                .fechaNacimiento(FECHA_NACIMIENTO)
                .build();
    }

    /**
     * ClienteRequestDTO no expone setters ni constructor con argumentos,
     * por eso los campos se cargan por reflexion y luego se serializan a JSON.
     */
    private String jsonRequestValido() throws Exception {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        ReflectionTestUtils.setField(dto, "email", EMAIL);
        ReflectionTestUtils.setField(dto, "password", "secreto123");
        ReflectionTestUtils.setField(dto, "dni", DNI);
        ReflectionTestUtils.setField(dto, "nombre", "Nombre");
        ReflectionTestUtils.setField(dto, "apellido", "Apellido");
        ReflectionTestUtils.setField(dto, "telefono", "1122334455");
        ReflectionTestUtils.setField(dto, "fechaNacimiento", FECHA_NACIMIENTO);
        return objectMapper.writeValueAsString(dto);
    }

    /**
     * ClienteUpdateDTO no expone setters ni constructor con argumentos, por eso
     * los campos se cargan por reflexion. La password se omite porque es opcional
     * en la actualizacion.
     */
    private String jsonUpdateValido() throws Exception {
        ClienteUpdateDTO dto = new ClienteUpdateDTO();
        ReflectionTestUtils.setField(dto, "email", EMAIL);
        ReflectionTestUtils.setField(dto, "dni", DNI);
        ReflectionTestUtils.setField(dto, "nombre", "Nombre");
        ReflectionTestUtils.setField(dto, "apellido", "Apellido");
        ReflectionTestUtils.setField(dto, "telefono", "1122334455");
        ReflectionTestUtils.setField(dto, "fechaNacimiento", FECHA_NACIMIENTO);
        return objectMapper.writeValueAsString(dto);
    }
}
