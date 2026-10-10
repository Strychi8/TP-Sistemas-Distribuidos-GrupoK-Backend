package com.rentar.customer_service.service.impl;

import com.rentar.customer_service.dto.request.LoginRequestDTO;
import com.rentar.customer_service.dto.response.AuthResponseDTO;
import com.rentar.customer_service.exception.custom.ResourceNotFoundException;
import com.rentar.customer_service.model.TokenBlacklist;
import com.rentar.customer_service.model.Usuario;
import com.rentar.customer_service.repository.ITokenBlacklistRepository;
import com.rentar.customer_service.repository.IUsuarioRepository;
import com.rentar.customer_service.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

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

        return AuthResponseDTO.builder()
                .token(jwtService.generateToken(user))
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(ur -> ur.getRol().getNombreRol().name()).collect(Collectors.toList()))
                .build();
    }

    /**
     * Recibe el JWT crudo (sin prefijo "Bearer "): en Hito 1 ese prefijo era parte
     * del header HTTP Authorization, pero H2-4 expone logout exclusivamente vía
     * gRPC (LogoutRequest.token ya es el JWT, sin convenciones de header HTTP).
     */
    @Override
    @Transactional
    public void logout(String token) {
        try {
            if (token == null || token.isBlank() || tokenBlacklistRepository.existsByToken(token)) {
                return;
            }

            Date expiration = jwtService.extractExpiration(token);

            LocalDateTime exp = expiration.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            TokenBlacklist tokenBlacklist = TokenBlacklist.builder()
                    .token(token)
                    .fechaExpiracion(exp)
                    .build();

            tokenBlacklistRepository.save(tokenBlacklist);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Si ya expiró, no es necesario agregarlo a la blacklist (o ya no representa riesgo)
        } catch (Exception e) {
            // Cualquier otro error de parseo de JWT, lo ignoramos para no romper el flujo de logout
        } finally {
            SecurityContextHolder.clearContext();
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
