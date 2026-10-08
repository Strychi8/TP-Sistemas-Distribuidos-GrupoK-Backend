package com.rentar.customer_service.config;

import io.grpc.Attributes;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Núcleo idéntico en los tres servicios. Única sección editable:
 * el bloque "// ── REGLAS DE ESTE SERVICIO ──".
 */
@Configuration
public class GrpcSecurityConfig {
    /** Las claves gRPC deben ser ASCII minúsculas (convención 4 del plan). */
    private static final Metadata.Key<String> ROLES =
            Metadata.Key.of("roles", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> SUB =
            Metadata.Key.of("sub", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * El interceptor global. El patrón oficial es: inyectar GrpcSecurity y devolver
     * el interceptor desde un @Bean con @GlobalServerInterceptor.
     */
    @Bean
    @GlobalServerInterceptor
    AuthenticationProcessInterceptor customerSecurityInterceptor(GrpcSecurity grpc) throws Exception {
        return grpc
                // Token que YA viene validado por el Gateway: este servicio no revalida JWT (ADR-001).
                .authenticationProvider(new GatewayIdentityAuthenticationProvider())
                .authenticationExtractor(this::extractIdentity)
                .authorizeRequests(requests -> {
                    // ── INFRAESTRUCTURA ──
                    // Reflection y healthcheck: necesarios para `grpcurl list`.
                    // Patrón contra el full method name, p.ej. grpc.reflection.v1.ServerReflection/...
                    requests.methods("grpc.*/*").permitAll();

                    // ── REGLAS DE ESTE SERVICIO ──
                    // Primera regla que matchea gana → de específica a general.

                    // Única excepción del fail-closed: en un login no puede haber identidad previa
                    // (equivale al permitAll de /api/auth/** del Hito 1).
                    requests.methods("rentar.customer.CustomerService/Login").permitAll();

                    // ADMINISTRADOR.
                    requests.methods(
                                    "rentar.customer.CustomerService/CrearCliente",
                                    "rentar.customer.CustomerService/ActualizarCliente",
                                    "rentar.customer.CustomerService/EliminarCliente",
                                    "rentar.customer.CustomerService/GetClientes",
                                    "rentar.customer.CustomerService/GetClientesActivos",
                                    "rentar.customer.CustomerService/GetClienteById")
                            .hasAnyAuthority("ADMINISTRADOR");

                    // Regla de negocio: CLIENTE o ADMINISTRADOR.
                    requests.methods("rentar.customer.CustomerService/GetMiPerfil")
                            .hasAnyAuthority("ADMINISTRADOR", "CLIENTE");

                    // RPCs internos del Gateway, ya autenticados → solo metadata presente.
                    requests.methods(
                                    "rentar.customer.CustomerService/VerificarClienteExistente",
                                    "rentar.customer.CustomerService/VerificarClienteActivo",
                                    "rentar.customer.CustomerService/VerificarTokenBlacklist",
                                    "rentar.customer.CustomerService/Logout")
                            .authenticated();

                    // Un RPC nuevo sin regla se rechaza solo.
                    requests.allRequests().denyAll();
                })
                .build();
    }

    /**
     * Lee la metadata de identidad que inyecta el Gateway.
     * - `roles`: string separada por comas, ej. "CLIENTE" o "CLIENTE, ADMINISTRADOR"
     * - `sub`: email del solicitante
     * Si `roles` falta o viene vacía → devuelve null → el interceptor siente la llamada
     * como anónima (AnonymousAuthenticationToken / ROLE_ANONYMOUS) → denyAll() la rechaza.
     */
    private Authentication extractIdentity(Metadata headers, Attributes attributes,
                                           MethodDescriptor<?, ?> method) {
        String roles = headers.get(ROLES);
        if (roles == null || roles.isBlank()) {
            return null;
        }
        String sub = headers.get(SUB);

        // Authorities SIN prefijo ROLE_: los valores vienen de Rol.NombreRol.
        // Normalizamos a mayúsculas: SimpleGrantedAuthority compara case-sensitive y el
        // claim debe viajar como CLIENTE,ADMINISTRADOR. Con la forma canónica no cambia nada;
        // con cualquier otra casing no se rompe la autorización.
        List<GrantedAuthority> authorities = Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(r -> !r.isEmpty())
                .map(r -> r.toUpperCase(Locale.ROOT))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new GatewayIdentityAuthentication(sub, authorities);
    }

    /**
     * Token de identidad del Gateway. No lleva credenciales: la validación de la firma
     * la hizo el Gateway, este servicio solo consume las authorities resultantes.
     */
    static final class GatewayIdentityAuthentication extends AbstractAuthenticationToken {

        private final String sub;

        GatewayIdentityAuthentication(String sub, List<GrantedAuthority> authorities) {
            super(authorities);
            this.sub = (sub == null || sub.isBlank()) ? "unknown" : sub;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() { return ""; }

        @Override
        public Object getPrincipal() { return this.sub; }
    }

    /**
     * El AuthenticationManager del interceptor llama authenticate() sobre lo que devuelve
     * el extractor. Como ya viene validado, devuelve el token tal cual.
     * (Si no registrás un provider que soporte el token, el ProviderManager lanza
     * ProviderNotFoundException → AuthenticationException → UNAUTHENTICATED.)
     */
    static final class GatewayIdentityAuthenticationProvider implements AuthenticationProvider {

        @Override
        public Authentication authenticate(Authentication authentication)
                throws AuthenticationException {
            return authentication;
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return GatewayIdentityAuthentication.class.isAssignableFrom(authentication);
        }
    }
}