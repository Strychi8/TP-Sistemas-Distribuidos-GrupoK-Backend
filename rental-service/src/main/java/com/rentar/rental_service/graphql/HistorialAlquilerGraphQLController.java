package com.rentar.rental_service.graphql;

import com.rentar.rental_service.dto.response.ReservaGraphQLDTO;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
//@PreAuthorize("hasAuthority('CLIENTE')")
public class HistorialAlquilerGraphQLController {

    private final IReservaService reservaService;

    public HistorialAlquilerGraphQLController(IReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @QueryMapping
    public List<ReservaGraphQLDTO> historialAlquileres(
            @Argument Long idCliente) {

        return reservaService.consultarHistorial(idCliente);
    }
}