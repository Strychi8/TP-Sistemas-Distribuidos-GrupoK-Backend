package com.empresa_rentar.web_services.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoginResponseDTO {
    private String token;
    private String type;
    private Long idUsuario;
    private String email;
    private List<String> roles;
    private Long clienteId;
}
