package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.enums.NombreRol;
import com.empresa_rentar.web_services.model.Rol;
import com.empresa_rentar.web_services.model.Usuario;
import com.empresa_rentar.web_services.model.UsuarioRol;
import com.empresa_rentar.web_services.model.UsuarioRolId;
import com.empresa_rentar.web_services.repository.IUsuarioRolRepository;
import com.empresa_rentar.web_services.service.IRolService;
import com.empresa_rentar.web_services.service.IUsuarioRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service("usuarioRolService")
public class UsuarioRolServiceImpl implements IUsuarioRolService {
    private final IUsuarioRolRepository usuarioRolRepository;
    private final IRolService rolService;

    @Override
    public void asignarRol(Usuario usuario, NombreRol nombreRol) {
        Rol rol = rolService.findByNombreRol(nombreRol);

        UsuarioRolId usuarioRolId = new UsuarioRolId();
        usuarioRolId.setUsuarioId(usuario.getIdUsuario());
        usuarioRolId.setRolId(rol.getIdRol());

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setId(usuarioRolId);
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);

        usuarioRolRepository.save(usuarioRol);
    }
}
