package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.response.ClienteResponseDTO;
import com.empresa_rentar.web_services.service.IClienteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "ABM de clientes")
public class ClienteController {
    private final IClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crearCliente (@Valid @RequestBody ClienteRequestDTO request, UriComponentsBuilder uriBuilder) {
        ClienteResponseDTO clienteResponse = clienteService.crearCliente(request);
        var uri = uriBuilder.path("/api/clientes/{id}").buildAndExpand(clienteResponse.getIdCliente()).toUri();
        return ResponseEntity.created(uri).body(clienteResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente (@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO request){
        return ResponseEntity.ok().body(clienteService.actualizarCliente(id, request));
    }



}
