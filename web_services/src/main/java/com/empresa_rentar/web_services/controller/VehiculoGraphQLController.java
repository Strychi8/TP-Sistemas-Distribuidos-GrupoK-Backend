package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.response.VehiculoResponseDTO;
import com.empresa_rentar.web_services.service.VehiculoService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class VehiculoGraphQLController {

    private final VehiculoService vehiculoService;

    public VehiculoGraphQLController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @QueryMapping
    public List<VehiculoResponseDTO> vehiculos() {
        return vehiculoService.listarVehiculos();
    }
}
