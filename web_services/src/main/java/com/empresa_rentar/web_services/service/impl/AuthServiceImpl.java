package com.empresa_rentar.web_services.service.impl;

import com.empresa_rentar.web_services.config.JwtService;
import com.empresa_rentar.web_services.dto.request.LoginRequestDTO;
import com.empresa_rentar.web_services.dto.response.AuthResponseDTO;
import com.empresa_rentar.web_services.exception.custom.ResourceNotFoundException;
import com.empresa_rentar.web_services.model.TokenBlacklist;
import com.empresa_rentar.web_services.model.Usuario;
import com.empresa_rentar.web_services.repository.ITokenBlacklistRepository;
import com.empresa_rentar.web_services.repository.IUsuarioRepository;
import com.empresa_rentar.web_services.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ITokenBlacklistRepository tokenBlacklistRepository;

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Usuario user = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String jwtToken = jwtService.generateToken(user);

        return AuthResponseDTO.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            
            try {
                if (tokenBlacklistRepository.existsByToken(jwt)) {
                    SecurityContextHolder.clearContext();
                    return;
                }

                Date expiration = jwtService.extractExpiration(jwt);
                
                LocalDateTime exp = expiration.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
                
                TokenBlacklist tokenBlacklist = TokenBlacklist.builder()
                        .token(jwt)
                        .fechaExpiracion(exp)
                        .build();
                
                tokenBlacklistRepository.save(tokenBlacklist);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Si ya expiró, no es necesario agregarlo a la blacklist (o ya no representa riesgo)
            } catch (Exception e) {
                // Cualquier otro error de parseo de JWT, lo ignoramos para no tirar 500
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        return null;
    }
}
