package com.rentar.customer_service.service.impl;

import com.rentar.customer_service.enums.NombreRol;
import com.rentar.customer_service.model.Rol;
import com.rentar.customer_service.model.Usuario;
import com.rentar.customer_service.model.UsuarioRol;
import com.rentar.customer_service.model.UsuarioRolId;
import com.rentar.customer_service.repository.IUsuarioRolRepository;
import com.rentar.customer_service.service.IRolService;
import com.rentar.customer_service.service.IUsuarioRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
