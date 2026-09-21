package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.request.FiltroReservaDTO;
import com.empresa_rentar.web_services.dto.response.ReservaGraphQLDTO;
import com.empresa_rentar.web_services.service.IReservaService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMINISTRADOR')")
public class ReservaGraphQLController {

    private final IReservaService reservaService;

    public ReservaGraphQLController(IReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @QueryMapping
    public List<ReservaGraphQLDTO> reservas(@Argument FiltroReservaDTO filtro) {
        return reservaService.consultarReservas(filtro);
    }
}
