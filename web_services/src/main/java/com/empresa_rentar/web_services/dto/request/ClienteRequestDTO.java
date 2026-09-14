package com.empresa_rentar.web_services.dto.request;

import com.empresa_rentar.web_services.validation.MayorDeEdad;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ClienteRequestDTO {
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
    @NotBlank(message = "El DNI es obligatorio")
    private String dni;
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @MayorDeEdad(message = "El cliente debe ser mayor de 18 años")
    private LocalDate fechaNacimiento;
}
