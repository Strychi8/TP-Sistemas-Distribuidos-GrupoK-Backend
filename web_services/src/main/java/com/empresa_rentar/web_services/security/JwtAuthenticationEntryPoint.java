package com.empresa_rentar.web_services.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Punto de entrada de autenticacion que se invoca cuando un request no autenticado
 * accede a un recurso protegido.
 *
 * <p>En una aplicacion stateless con JWT, este componente reemplaza al formulario de login
 * tradicional. Cuando el {@link JwtAuthenticationFilter} no encuentra un token valido
 * (o no hay token), el SecurityContext queda sin autenticacion, y cuando el
 * {@code SecurityFilterChain} intenta autorizar el request, Spring Security invoca
 * este metodo.</p>
 *
 * <p><b>Por que se usa ObjectMapper manual?</b> Porque este punto de entrada se ejecuta
 * ANTES de que el request llegue al {@code @RestControllerAdvice} ({@code GlobalExceptionHandler}),
 * por lo que no podemos usar las excepciones custom del proyecto. Retornamos el mismo formato
 * JSON que {@code ErrorResponseDTO} para mantener consistencia en el cliente.</p>
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 401,
                "error", "Unauthorized",
                "message", "No se proporciono token de autenticacion o el token es invalido/expirado",
                "path", request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
