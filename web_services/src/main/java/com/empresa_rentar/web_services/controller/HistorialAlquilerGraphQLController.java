package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.response.HistorialAlquilerResponseDTO;
import com.empresa_rentar.web_services.service.IReservaService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@PreAuthorize("hasAuthority('CLIENTE')")
public class HistorialAlquilerGraphQLController {

    private final IReservaService reservaService;

    public HistorialAlquilerGraphQLController(IReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @QueryMapping
    public List<HistorialAlquilerResponseDTO> historialAlquileres(
            @Argument Long idCliente) {

        return reservaService.consultarHistorial(idCliente);
    }
}