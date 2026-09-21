package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.response.ReservaGraphQLDTO;
import com.empresa_rentar.web_services.service.IReservaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@GraphQlTest(ReservaGraphQLController.class) // Solo carga lo relacionado a GraphQL
class ReservaGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester; // Utilidad nativa para testear queries

    @MockitoBean
    private IReservaService reservaService; // Simulamos el servicio para no tocar BD real

    @Test
    void testQueryReservas() {
        // 1. Prepare
        ReservaGraphQLDTO mockReserva = ReservaGraphQLDTO.builder()
                .idReserva(1L)
                .importeTotal(new java.math.BigDecimal("50000.00"))
                .build();
                
        Mockito.when(reservaService.consultarReservas(any())).thenReturn(List.of(mockReserva));

        // 2. Query (La petición tal cual la harías en Postman o Apollo)
        String document = """
                query {
                  reservas {
                    idReserva
                    importeTotal
                  }
                }
                """;

        // 3. Act & Assert
        graphQlTester.document(document)
                .execute()
                .path("reservas") // Busca en el JSON la key "reservas"
                .entityList(ReservaGraphQLDTO.class) // Asegura que sea una lista
                .hasSize(1) // Valida la cantidad
                .path("reservas[0].idReserva")
                .entity(Long.class).isEqualTo(1L); // Valida un valor específico
    }
}
