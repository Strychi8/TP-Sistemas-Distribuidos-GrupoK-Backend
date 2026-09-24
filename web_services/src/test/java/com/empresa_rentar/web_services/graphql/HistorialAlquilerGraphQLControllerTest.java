package com.empresa_rentar.web_services.graphql;

import com.empresa_rentar.web_services.dto.response.HistorialAlquilerResponseDTO;
import com.empresa_rentar.web_services.enums.EstadoReserva;
import com.empresa_rentar.web_services.service.IReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

@GraphQlTest(HistorialAlquilerGraphQLController.class)
public class HistorialAlquilerGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private IReservaService reservaService;

    @Test
    void consultarHistorial_debeRetornarListaDeAlquileres() {
        // Arrange
        Long idCliente = 1L;
        String document = """
                query {
                    historialAlquileres(idCliente: 1) {
                        vehiculo
                        patente
                        fechaInicio
                        fechaFinalizacion
                        cantidadDias
                        importeTotal
                        estado
                    }
                }
                """;

        HistorialAlquilerResponseDTO mockResponse = new HistorialAlquilerResponseDTO(
                "Ford Fiesta",
                "XYZ987AB",
                "2023-10-01T10:00:00",
                "2023-10-05T10:00:00",
                4L,
                BigDecimal.valueOf(80000.00),
                EstadoReserva.FINALIZADA
        );

        when(reservaService.consultarHistorial(idCliente)).thenReturn(List.of(mockResponse));

        // Act & Assert
        graphQlTester.document(document)
                .execute()
                .path("historialAlquileres")
                .entityList(HistorialAlquilerResponseDTO.class)
                .hasSize(1)
                .contains(mockResponse);
    }
}
