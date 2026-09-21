package com.empresa_rentar.web_services.mapper;

import com.empresa_rentar.web_services.dto.request.ClienteRequestDTO;
import com.empresa_rentar.web_services.dto.response.ClienteResponseDTO;
import com.empresa_rentar.web_services.model.Cliente;
import com.empresa_rentar.web_services.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayName("ClienteMapper - tests unitarios")
class ClienteMapperTest {

    private static final Long ID_USUARIO = 10L;
    private static final String EMAIL = "cliente@test.com";
    private static final String DNI = "30111222";
    private static final String NOMBRE = "Nombre";
    private static final String APELLIDO = "Apellido";
    private static final String TELEFONO = "1122334455";
    private static final LocalDate FECHA_NACIMIENTO = LocalDate.of(1995, 3, 15);

    @Test
    @DisplayName("toCliente: copia todos los campos del request y asigna el usuario")
    void deberiaMapearRequestAEntidadCliente() {
        ClienteRequestDTO request = buildRequest();
        Usuario usuario = Usuario.builder().idUsuario(ID_USUARIO).email(EMAIL).build();

        Cliente cliente = ClienteMapper.toCliente(request, usuario);

        assertThat(cliente.getUsuario()).isEqualTo(usuario);
        assertThat(cliente.getDni()).isEqualTo(DNI);
        assertThat(cliente.getEmail()).isEqualTo(EMAIL);
        assertThat(cliente.getNombre()).isEqualTo(NOMBRE);
        assertThat(cliente.getApellido()).isEqualTo(APELLIDO);
        assertThat(cliente.getTelefono()).isEqualTo(TELEFONO);
        assertThat(cliente.getFechaNacimiento()).isEqualTo(FECHA_NACIMIENTO);
        assertThat(cliente.getActivo()).isTrue();
        assertThat(cliente.getIdCliente()).isNull();
    }

    @Test
    @DisplayName("toClienteResponseDTO: copia todos los campos incluyendo el idUsuario")
    void deberiaMapearClienteAResponseDTO() {
        Usuario usuario = Usuario.builder().idUsuario(ID_USUARIO).email(EMAIL).build();
        Cliente cliente = Cliente.builder()
                .idCliente(1L)
                .usuario(usuario)
                .dni(DNI)
                .nombre(NOMBRE)
                .apellido(APELLIDO)
                .email(EMAIL)
                .telefono(TELEFONO)
                .fechaNacimiento(FECHA_NACIMIENTO)
                .activo(true)
                .build();

        ClienteResponseDTO response = ClienteMapper.toClienteResponseDTO(cliente);

        assertThat(response.getIdCliente()).isEqualTo(1L);
        assertThat(response.getIdUsuario()).isEqualTo(ID_USUARIO);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getDni()).isEqualTo(DNI);
        assertThat(response.getNombre()).isEqualTo(NOMBRE);
        assertThat(response.getApellido()).isEqualTo(APELLIDO);
        assertThat(response.getTelefono()).isEqualTo(TELEFONO);
        assertThat(response.getFechaNacimiento()).isEqualTo(FECHA_NACIMIENTO);
    }

    @Test
    @DisplayName("toClienteResponseDTO: lanza NullPointerException si el cliente no tiene usuario")
    void deberiaLanzarNullPointerCuandoElClienteNoTieneUsuario() {
        Cliente clienteSinUsuario = Cliente.builder()
                .idCliente(1L)
                .dni(DNI)
                .nombre(NOMBRE)
                .apellido(APELLIDO)
                .email(EMAIL)
                .build();

        Throwable thrown = catchThrowable(() -> ClienteMapper.toClienteResponseDTO(clienteSinUsuario));

        assertThat(thrown).isInstanceOf(NullPointerException.class);
    }

    /**
     * ClienteRequestDTO no expone setters ni constructor con argumentos,
     * por eso los campos se cargan por reflexion.
     */
    private ClienteRequestDTO buildRequest() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        ReflectionTestUtils.setField(dto, "email", EMAIL);
        ReflectionTestUtils.setField(dto, "password", "secreto123");
        ReflectionTestUtils.setField(dto, "dni", DNI);
        ReflectionTestUtils.setField(dto, "nombre", NOMBRE);
        ReflectionTestUtils.setField(dto, "apellido", APELLIDO);
        ReflectionTestUtils.setField(dto, "telefono", TELEFONO);
        ReflectionTestUtils.setField(dto, "fechaNacimiento", FECHA_NACIMIENTO);
        return dto;
    }
}
