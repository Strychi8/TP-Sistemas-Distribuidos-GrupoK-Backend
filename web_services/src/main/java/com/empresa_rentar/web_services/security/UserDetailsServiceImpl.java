package com.empresa_rentar.web_services.security;

import com.empresa_rentar.web_services.enums.NombreRol;
import com.empresa_rentar.web_services.model.Cliente;
import com.empresa_rentar.web_services.model.Usuario;
import com.empresa_rentar.web_services.repository.IClienteRepository;
import com.empresa_rentar.web_services.service.IUsuarioRolService;
import com.empresa_rentar.web_services.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUsuarioService usuarioService;
    private final IClienteRepository clienteRepository;
    private final IUsuarioRolService usuarioRolService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email));

        List<NombreRol> roles = usuarioRolService.obtenerRolesDeUsuario(usuario.getIdUsuario());

        Long clienteId = clienteRepository
                .findByUsuario_IdUsuario(usuario.getIdUsuario())
                .map(Cliente::getIdCliente)
                .orElse(null);

        List<String> roleNames = roles.stream()
                .map(nombreRol -> "ROLE_" + nombreRol.name())
                .toList();

        return UserDetailsImpl.build(
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.getActivo(),
                clienteId,
                roleNames
        );
    }
}
