package com.rentar.rental_service.graphql;

import com.rentar.rental_service.dto.request.FiltroReservaDTO;
import com.rentar.rental_service.dto.response.ReservaGraphQLDTO;
import com.rentar.rental_service.service.IReservaService;
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
