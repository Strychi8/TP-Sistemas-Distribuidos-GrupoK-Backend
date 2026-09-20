package com.empresa_rentar.web_services.controller;

import com.empresa_rentar.web_services.dto.request.LoginRequestDTO;
import com.empresa_rentar.web_services.dto.response.LoginResponseDTO;
import com.empresa_rentar.web_services.security.JwtUtils;
import com.empresa_rentar.web_services.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Login y manejo de tokens JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesion",
            description = "Autentica un usuario con email y contrasena. "
                    + "Retorna un token JWT valido por 30 minutos que debe incluirse "
                    + "en el header Authorization como 'Bearer <token>'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso - retorna el token JWT y datos del usuario",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada invalidos (email o password vacios)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales invalidas o usuario desactivado",
                    content = @Content
            )
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtUtils.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .idUsuario(userDetails.getIdUsuario())
                .email(userDetails.getEmail())
                .roles(roles)
                .clienteId(userDetails.getClienteId())
                .build();

        return ResponseEntity.ok(response);
    }
}
