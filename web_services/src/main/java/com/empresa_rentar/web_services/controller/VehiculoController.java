package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.request.VehiculoRequestDTO;
import com.empresa_rentar.web_services.dto.request.VehiculoUpdateRequestDTO;
import com.empresa_rentar.web_services.dto.response.VehiculoResponseDTO;
import com.empresa_rentar.web_services.service.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@Tag(name = "Vehículos", description = "ABM de vehículos de la flota de Rentar [ADMINISTRADOR]")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @PostMapping
    @Operation(summary = "Crear vehículo", description = "Registra un nuevo vehículo. El estado inicial será DISPONIBLE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un vehículo con esa patente")
    })
    public ResponseEntity<VehiculoResponseDTO> crearVehiculo(@Valid @RequestBody VehiculoRequestDTO dto) {
        VehiculoResponseDTO creado = vehiculoService.crearVehiculo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar vehículo", description = "Actualiza los datos de un vehículo. La patente no puede modificarse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehículo actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<VehiculoResponseDTO> actualizarVehiculo(@PathVariable Long id,
                                                          @Valid @RequestBody VehiculoUpdateRequestDTO dto) {
        VehiculoResponseDTO actualizado = vehiculoService.actualizarVehiculo(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar vehículo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<VehiculoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todos los vehículos", description = "Devuelve todos los vehículos, activos e inactivos.")
    public ResponseEntity<List<VehiculoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(vehiculoService.listarTodos());
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar vehículos activos", description = "Devuelve solo los vehículos activos.")
    public ResponseEntity<List<VehiculoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(vehiculoService.listarActivos());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja vehículo", description = "Realiza una baja lógica del vehículo (activo = false).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vehículo dado de baja exitosamente"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        vehiculoService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
