package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.request.ClienteUpdateDTO;
import com.empresa_rentar.web_services.dto.response.ClienteResponseDTO;
import com.empresa_rentar.web_services.service.IClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "ABM de clientes")
public class ClienteController {
    private final IClienteService clienteService;

    @PostMapping
    @Operation(
            summary = "Crear un cliente",
            description = "Registra un nuevo cliente en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente creado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El DNI ya se encuentra registrado",
                    content = @Content
            )
    })
    public ResponseEntity<ClienteResponseDTO> crearCliente (@Valid @RequestBody ClienteRequestDTO request, UriComponentsBuilder uriBuilder) {
        ClienteResponseDTO clienteResponse = clienteService.crearCliente(request);
        var uri = uriBuilder.path("/api/clientes/{id}").buildAndExpand(clienteResponse.getIdCliente()).toUri();
        return ResponseEntity.created(uri).body(clienteResponse);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un cliente",
            description = "Actualiza los datos de un cliente existente mediante su identificador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente actualizado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El DNI ya se encuentra registrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El email ya se encuentra registrado"
            )
    })
    public ResponseEntity<ClienteResponseDTO> actualizarCliente (
            @Parameter(
                    description = "Identificador único del cliente",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,
            @Valid @RequestBody ClienteUpdateDTO request){
        return ResponseEntity.ok().body(clienteService.actualizarCliente(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un cliente",
            description = "Realiza una eliminación lógica del cliente. El cliente no se elimina físicamente de la base de datos, sino que pasa a estar inactivo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Cliente eliminado correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<Void> eliminarCliente (
            @Parameter(
                    description = "Identificador único del cliente",
                    example = "1",
                    required = true
            )
            @PathVariable Long id){
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Listar todos los clientes",
            description = "Obtiene todos los clientes registrados en el sistema, incluyendo los clientes activos e inactivos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado obtenido correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ClienteResponseDTO.class)
                            )
                    )
            )
    })
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes(){
        return ResponseEntity.ok().body(clienteService.listarClientes());
    }

    @GetMapping("/activos")
    @Operation(
            summary = "Listar clientes activos",
            description = "Obtiene únicamente los clientes que se encuentran activos en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de clientes activos obtenido correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ClienteResponseDTO.class)
                            )
                    )
            )
    })
    public ResponseEntity<List<ClienteResponseDTO>> listarClientesActivos(){
        return ResponseEntity.ok().body(clienteService.listarClientesActivos());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un cliente por ID",
            description = "Obtiene la información de un cliente mediante su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<ClienteResponseDTO> obtenerCliente(
            @Parameter(
                    description = "Identificador único del cliente",
                    example = "1",
                    required = true
            )
            @PathVariable Long id) {
        return ResponseEntity.ok().body(clienteService.obtenerClientePorId(id));
    }
}
