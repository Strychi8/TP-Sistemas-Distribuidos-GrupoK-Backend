package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.enums.NombreRol;
import com.empresa_rentar.web_services.exception.custom.RolNotFoundException;
import com.empresa_rentar.web_services.model.Rol;
import com.empresa_rentar.web_services.model.Usuario;
import com.empresa_rentar.web_services.model.UsuarioRol;
import com.empresa_rentar.web_services.repository.IUsuarioRolRepository;
import com.empresa_rentar.web_services.service.IRolService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioRolServiceImpl - tests unitarios")
class UsuarioRolServiceImplTest {

    private static final Long ID_USUARIO = 10L;
    private static final Long ID_ROL_CLIENTE = 2L;

    @Mock
    private IUsuarioRolRepository usuarioRolRepository;

    @Mock
    private IRolService rolService;

    @InjectMocks
    private UsuarioRolServiceImpl usuarioRolService;

    @Test
    @DisplayName("asignarRol: busca el rol y persiste la relacion usuario-rol")
    void deberiaAsignarRolAlUsuario() {
        Usuario usuario = Usuario.builder().idUsuario(ID_USUARIO).email("usuario@test.com").build();
        Rol rolCliente = Rol.builder().idRol(ID_ROL_CLIENTE).nombreRol(NombreRol.CLIENTE).build();
        when(rolService.findByNombreRol(NombreRol.CLIENTE)).thenReturn(rolCliente);

        usuarioRolService.asignarRol(usuario, NombreRol.CLIENTE);

        ArgumentCaptor<UsuarioRol> captor = ArgumentCaptor.forClass(UsuarioRol.class);
        verify(usuarioRolRepository).save(captor.capture());
        UsuarioRol persistido = captor.getValue();

        assertThat(persistido.getId()).isNotNull();
        assertThat(persistido.getId().getUsuarioId()).isEqualTo(ID_USUARIO);
        assertThat(persistido.getId().getRolId()).isEqualTo(ID_ROL_CLIENTE);
        assertThat(persistido.getUsuario()).isNotNull();
        assertThat(persistido.getUsuario().getIdUsuario()).isEqualTo(ID_USUARIO);
        assertThat(persistido.getRol()).isNotNull();
        assertThat(persistido.getRol().getIdRol()).isEqualTo(ID_ROL_CLIENTE);

        verify(rolService).findByNombreRol(NombreRol.CLIENTE);
    }

    @Test
    @DisplayName("asignarRol: propaga RolNotFoundException y no persiste nada cuando el rol no existe")
    void deberiaLanzarRolNotFoundCuandoElRolNoExiste() {
        Usuario usuario = Usuario.builder().idUsuario(ID_USUARIO).email("usuario@test.com").build();
        when(rolService.findByNombreRol(NombreRol.ADMINISTRADOR)).thenThrow(new RolNotFoundException());

        assertThatThrownBy(() -> usuarioRolService.asignarRol(usuario, NombreRol.ADMINISTRADOR))
                .isInstanceOf(RolNotFoundException.class)
                .hasMessage("Rol no encontrado");

        verifyNoInteractions(usuarioRolRepository);
    }
}
