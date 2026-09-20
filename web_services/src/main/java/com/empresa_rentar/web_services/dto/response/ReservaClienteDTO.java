package com.empresa_rentar.web_services.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservaClienteDTO {
    private Long idCliente;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
}
