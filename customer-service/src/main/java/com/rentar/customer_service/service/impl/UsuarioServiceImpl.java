package com.rentar.customer_service.service.impl;

import com.rentar.customer_service.exception.custom.EmailAlreadyExistsException;
import com.rentar.customer_service.model.Usuario;
import com.rentar.customer_service.repository.IUsuarioRepository;
import com.rentar.customer_service.service.IUsuarioService;
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

    @Override
    public void actualizarUsuario(Usuario usuario, String email, String password) {
        usuario.setEmail(email);
        if (password != null && !password.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }
    }
}
