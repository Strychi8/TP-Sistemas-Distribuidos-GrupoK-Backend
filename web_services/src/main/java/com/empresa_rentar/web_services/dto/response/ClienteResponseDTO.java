package com.empresa_rentar.web_services.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter @Getter
@Builder
public class ClienteResponseDTO {
    private Long idCliente;
    private Long idUsuario;
    private String email;
    private String dni;
    private String nombre;
    private String apellido;
    private String telefono;
    private LocalDate fechaNacimiento;
    private Boolean activo;
}
