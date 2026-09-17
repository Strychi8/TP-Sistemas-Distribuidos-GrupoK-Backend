package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.enums.NombreRol;
import com.empresa_rentar.web_services.exception.custom.RolNotFoundException;
import com.empresa_rentar.web_services.model.Rol;
import com.empresa_rentar.web_services.repository.IRolRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RolServiceImpl - tests unitarios")
class RolServiceImplTest {

    @Mock
    private IRolRepository rolRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    @Test
    @DisplayName("existsByNombreRol: delega en el repositorio y devuelve true cuando existe")
    void deberiaDevolverTrueCuandoElRolExiste() {
        when(rolRepository.existsByNombreRol(NombreRol.CLIENTE)).thenReturn(true);

        assertThat(rolService.existsByNombreRol(NombreRol.CLIENTE)).isTrue();
        verify(rolRepository).existsByNombreRol(NombreRol.CLIENTE);
    }

    @Test
    @DisplayName("existsByNombreRol: devuelve false cuando el rol no existe")
    void deberiaDevolverFalseCuandoElRolNoExiste() {
        when(rolRepository.existsByNombreRol(NombreRol.ADMINISTRADOR)).thenReturn(false);

        assertThat(rolService.existsByNombreRol(NombreRol.ADMINISTRADOR)).isFalse();
        verify(rolRepository).existsByNombreRol(NombreRol.ADMINISTRADOR);
    }

    @Test
    @DisplayName("findByNombreRol: devuelve el rol cuando existe")
    void deberiaDevolverElRolCuandoExiste() {
        Rol rol = Rol.builder().idRol(2L).nombreRol(NombreRol.CLIENTE).build();
        when(rolRepository.findByNombreRol(NombreRol.CLIENTE)).thenReturn(Optional.of(rol));

        Rol encontrado = rolService.findByNombreRol(NombreRol.CLIENTE);

        assertThat(encontrado).isEqualTo(rol);
        assertThat(encontrado.getIdRol()).isEqualTo(2L);
        assertThat(encontrado.getNombreRol()).isEqualTo(NombreRol.CLIENTE);
    }

    @Test
    @DisplayName("findByNombreRol: lanza RolNotFoundException cuando no existe")
    void deberiaLanzarRolNotFoundCuandoNoExiste() {
        when(rolRepository.findByNombreRol(NombreRol.ADMINISTRADOR)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolService.findByNombreRol(NombreRol.ADMINISTRADOR))
                .isInstanceOf(RolNotFoundException.class)
                .hasMessage("Rol no encontrado");
    }
}
