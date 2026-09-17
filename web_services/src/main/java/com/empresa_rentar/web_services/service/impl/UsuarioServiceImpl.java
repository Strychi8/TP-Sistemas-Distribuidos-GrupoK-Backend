package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.exception.custom.EmailAlreadyExistsException;
import com.empresa_rentar.web_services.model.Usuario;
import com.empresa_rentar.web_services.repository.IUsuarioRepository;
import com.empresa_rentar.web_services.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("usuarioService")
public class UsuarioServiceImpl implements IUsuarioService {
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public Usuario crearUsuario(String email, String password) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }
        Usuario usuario = new Usuario();

        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }


}
