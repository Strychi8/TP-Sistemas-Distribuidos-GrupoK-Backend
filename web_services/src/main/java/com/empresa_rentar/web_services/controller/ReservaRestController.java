package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.request.ReservaRequestDTO;
import com.empresa_rentar.web_services.dto.response.ReservaResponseDTO;
import com.empresa_rentar.web_services.service.IReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Endpoints para la gestión transaccional de reservas de vehículos")
public class ReservaRestController {

    private final IReservaService reservaService;


    @PostMapping
    @Operation(summary = "Alta de Reserva",
            description = "Registra una nueva reserva confirmada para un vehículo y cliente activo," +
                    " calculando el importe total y verificando disponibilidad de fechas ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Reserva creada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Regla de negocio violada (fechas solapadas, cliente/vehículo inactivo o rango inválido)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404",
                    description = "Cliente o vehículo no encontrado",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ReservaResponseDTO> createReserva(@Valid @RequestBody ReservaRequestDTO dto) {
        ReservaResponseDTO creada = reservaService.crearReserva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar Reserva",
            description = "Realiza la baja lógica de una reserva activa modificando su estado a CANCELADA," +
                    " siempre que el período de alquiler aún no haya iniciado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Reserva cancelada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "El período de alquiler ya se encuentra en curso o la reserva ya fue cancelada",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(@PathVariable Long id) {
        ReservaResponseDTO cancelada = reservaService.cancelarReserva(id);
        return ResponseEntity.ok(cancelada);
    }

    @GetMapping("/{id}")
    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar Reserva",
            description = "Realiza la baja lógica de una reserva activa modificando su estado a CANCELADA," +
                    " siempre que el período de alquiler aún no haya iniciado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Reserva cancelada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "El período de alquiler ya se encuentra en curso o la reserva ya fue cancelada",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable("id") Long id) {
        ReservaResponseDTO respuesta = reservaService.obtenerPorId(id);
        return ResponseEntity.ok(respuesta);
    }


}
