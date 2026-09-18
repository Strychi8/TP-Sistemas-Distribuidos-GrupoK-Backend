package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.request.VehiculoDisponibilidadRequestDTO;
import com.empresa_rentar.web_services.dto.response.VehiculoResponseDTO;
import com.empresa_rentar.web_services.service.VehiculoService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @QueryMapping
    public List<VehiculoResponseDTO> vehiculosDisponibles(@Argument String inicio,
                                                          @Argument String fin,
                                                          @Argument VehiculoDisponibilidadRequestDTO filtro) {
        LocalDateTime fechaInicio = LocalDateTime.parse(inicio, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime fechaFin = LocalDateTime.parse(fin);
        return vehiculoService.consultarDisponibilidad(fechaInicio, fechaFin, filtro);
    }
}
