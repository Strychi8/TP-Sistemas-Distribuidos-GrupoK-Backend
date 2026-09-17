package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.service.IClienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests del GlobalExceptionHandler ejercitados a traves del ClienteController
 * (slice web). Cubren los mapeos de excepciones a codigos HTTP.
 */
@WebMvcTest(ClienteController.class)
@ActiveProfiles("test")
@DisplayName("GlobalExceptionHandler - tests de mapeo de errores HTTP")
class GlobalExceptionHandlerTest {

    private static final String JSON_VALIDO = """
            {
              "email": "cliente@test.com",
              "password": "secreto123",
              "dni": "30111222",
              "nombre": "Nombre",
              "apellido": "Apellido",
              "telefono": "1122334455",
              "fechaNacimiento": "1995-03-15"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IClienteService clienteService;

    @Test
    @DisplayName("POST /api/clientes: mapea DataIntegrityViolationException a 409 Conflict")
    void deberiaResponder409AnteViolacionDeIntegridadDeDatos() throws Exception {
        when(clienteService.crearCliente(any(ClienteRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("duplicado"));

        mockMvc.perform(post("/api/clientes")
                        .contentType(APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Se produjo una violación de integridad de datos. Verifique que los datos enviados no interfieran con restricciones existentes."));
    }

    @Test
    @DisplayName("POST /api/clientes: responde 400 cuando el JSON del cuerpo es malformado")
    void deberiaResponder400AnteJsonMalformado() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(APPLICATION_JSON)
                        .content("{ esta-no-es-una-json valida "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed JSON Error"))
                .andExpect(jsonPath("$.message").value(
                        "El cuerpo de la solicitud no es válido. Verifique que el JSON esté correctamente formado y que los tipos de datos sean correctos."));
    }

    @Test
    @DisplayName("GET /api/clientes/{id}: responde 400 cuando el id no es numerico")
    void deberiaResponder400AnteParametroDeTipoInvalido() throws Exception {
        mockMvc.perform(get("/api/clientes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Parameter"))
                .andExpect(jsonPath("$.message").value(
                        "El parámetro 'id' tiene un valor inválido: 'abc'. Se esperaba un valor de tipo Long."));
    }

    @Test
    @DisplayName("GET /api/clientes/{id}: mapea errores inesperados a 500")
    void deberiaResponder500AnteErrorInesperado() throws Exception {
        when(clienteService.obtenerClientePorId(1L)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value(
                        "Ha ocurrido un error inesperado. Contacte al administrador."));
    }
}