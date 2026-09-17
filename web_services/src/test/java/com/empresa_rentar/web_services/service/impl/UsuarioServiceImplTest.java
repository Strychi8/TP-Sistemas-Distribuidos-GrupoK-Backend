package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.exception.custom.EmailAlreadyExistsException;
import com.empresa_rentar.web_services.model.Usuario;
import com.empresa_rentar.web_services.repository.IUsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceImpl - tests unitarios")
class UsuarioServiceImplTest {

    private static final String EMAIL = "usuario@test.com";
    private static final String PASSWORD = "secreto123";
    private static final String PASSWORD_ENCRIPTADA = "$2a$10$abcdefghijklmnopqrstuv";

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    @DisplayName("crearUsuario: lanza EmailAlreadyExistsException cuando el email ya esta registrado")
    void deberiaLanzarEmailAlreadyExistsCuandoElEmailYaExiste() {
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crearUsuario(EMAIL, PASSWORD))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("El email ya está asignado a otro usuario");

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("crearUsuario: guarda la password encriptada con BCrypt y el usuario activo")
    void deberiaCrearUsuarioConPasswordEncriptada() {
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_ENCRIPTADA);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario creado = usuarioService.crearUsuario(EMAIL, PASSWORD);

        assertThat(creado.getEmail()).isEqualTo(EMAIL);
        assertThat(creado.getPassword()).isEqualTo(PASSWORD_ENCRIPTADA);
        assertThat(creado.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(creado.getActivo()).isTrue();

        verify(passwordEncoder).encode(PASSWORD);
        verify(usuarioRepository).save(creado);
    }

    @Test
    @DisplayName("actualizarUsuario: actualiza el email y no toca la password cuando llega null")
    void deberiaActualizarEmailSinTocarPasswordCuandoLlegaNull() {
        Usuario usuario = Usuario.builder()
                .idUsuario(1L)
                .email("viejo@test.com")
                .password(PASSWORD_ENCRIPTADA)
                .build();

        usuarioService.actualizarUsuario(usuario, EMAIL, null);

        assertThat(usuario.getEmail()).isEqualTo(EMAIL);
        assertThat(usuario.getPassword()).isEqualTo(PASSWORD_ENCRIPTADA);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("actualizarUsuario: actualiza el email y no toca la password cuando llega en blanco")
    void deberiaActualizarEmailSinTocarPasswordCuandoLlegaEnBlanco() {
        Usuario usuario = Usuario.builder()
                .idUsuario(1L)
                .email("viejo@test.com")
                .password(PASSWORD_ENCRIPTADA)
                .build();

        usuarioService.actualizarUsuario(usuario, EMAIL, "   ");

        assertThat(usuario.getEmail()).isEqualTo(EMAIL);
        assertThat(usuario.getPassword()).isEqualTo(PASSWORD_ENCRIPTADA);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("actualizarUsuario: actualiza el email y reencripta la password cuando viene una nueva")
    void deberiaActualizarEmailYPasswordCuandoVienePasswordNueva() {
        Usuario usuario = Usuario.builder()
                .idUsuario(1L)
                .email("viejo@test.com")
                .password(PASSWORD_ENCRIPTADA)
                .build();
        String passwordNueva = "nueva123";
        String passwordNuevaEncriptada = "$2a$10$xyzabcXYZABC123";
        when(passwordEncoder.encode(passwordNueva)).thenReturn(passwordNuevaEncriptada);

        usuarioService.actualizarUsuario(usuario, EMAIL, passwordNueva);

        assertThat(usuario.getEmail()).isEqualTo(EMAIL);
        assertThat(usuario.getPassword()).isEqualTo(passwordNuevaEncriptada);
        verify(passwordEncoder).encode(passwordNueva);
    }
}
