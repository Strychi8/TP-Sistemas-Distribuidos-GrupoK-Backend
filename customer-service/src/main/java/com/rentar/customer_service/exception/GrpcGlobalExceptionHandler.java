package com.rentar.customer_service.exception;

import com.rentar.customer_service.exception.custom.BadRequestException;
import com.rentar.customer_service.exception.custom.ClienteNotFoundException;
import com.rentar.customer_service.exception.custom.DniAlreadyExistsException;
import com.rentar.customer_service.exception.custom.EmailAlreadyExistsException;
import com.rentar.customer_service.exception.custom.ResourceNotFoundException;
import io.grpc.Status;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.grpc.server.advice.GrpcAdvice;
import org.springframework.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

/**
 * Equivalente gRPC de {@link GlobalExceptionHandler} (HTTP): traduce las excepciones
 * de dominio de la capa de servicio a códigos {@link Status} estándar de gRPC.
 *
 * El starter de Spring gRPC (spring-boot-starter-grpc-server) descubre automáticamente
 * las clases anotadas con {@code @GrpcAdvice} y sus métodos {@code @GrpcExceptionHandler}
 * (vía GrpcAdviceDiscoverer), y registra un único GrpcExceptionHandlerInterceptor global
 * que los invoca. No hace falta cablear nada manualmente en GrpcSecurityConfig ni en
 * ningún otro lugar: alcanza con que este bean exista en el contexto.
 *
 * El método con la excepción más específica siempre gana (ExceptionDepthComparator),
 * igual que @ExceptionHandler en @RestControllerAdvice.
 */
@GrpcAdvice
public class GrpcGlobalExceptionHandler {

    /**
     * El cliente no existe (buscarClientePorId en ClienteServiceImpl).
     */
    @GrpcExceptionHandler(ClienteNotFoundException.class)
    public Status handleClienteNotFound(ClienteNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage());
    }

    /**
     * Caso borde de AuthServiceImpl.login: la autenticación ya fue exitosa pero el
     * Usuario no se encuentra en la tabla (inconsistencia de datos). Distinto de
     * credenciales inválidas (ver handleAuthenticationException).
     */
    @GrpcExceptionHandler(ResourceNotFoundException.class)
    public Status handleResourceNotFound(ResourceNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage());
    }

    /**
     * AuthServiceImpl.login lanza esto (vía AuthenticationManager) específicamente
     * cuando el email o la contraseña no son válidos. Mismo mensaje que el 401 de
     * Hito 1 (GlobalExceptionHandler.handleAuthenticationException) para no perder
     * paridad. Es más específico que AuthenticationException (ver el handler de abajo)
     * así que gana cuando el login falla por credenciales incorrectas.
     */
    @GrpcExceptionHandler(BadCredentialsException.class)
    public Status handleBadCredentials(BadCredentialsException ex) {
        return Status.UNAUTHENTICATED.withDescription("Credenciales inválidas. Verifique su email y contraseña.");
    }

    /**
     * Catch-all para cualquier otra AuthenticationException: en la práctica, la que
     * lanza GrpcSecurityConfig cuando un RPC que requiere authenticated() (p. ej.
     * VerificarTokenBlacklist, Logout) se invoca sin metadata de identidad (roles/sub)
     * o con una identidad que el AuthenticationProvider no reconoce. No confundir con
     * handleBadCredentials: aquí no hubo intento de login, faltó la identidad en sí.
     */
    @GrpcExceptionHandler(AuthenticationException.class)
    public Status handleAuthenticationException(AuthenticationException ex) {
        return Status.UNAUTHENTICATED.withDescription(
                "No autenticado: esta operación requiere una identidad autenticada.");
    }

    /**
     * Validaciones de negocio sobre un cliente que sí existe: cubre tanto "cliente
     * inactivo" como "cliente menor de edad" (ClienteServiceImpl.verificarClienteActivo
     * lanza esta misma excepción en ambos casos, conservando los mensajes exactos
     * del Hito 1, p. ej. "El cliente debe ser mayor de 18 años").
     */
    @GrpcExceptionHandler(BadRequestException.class)
    public Status handleBadRequest(BadRequestException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
    }

    /**
     * Fallback para cualquier excepción no mapeada explícitamente arriba. Sin este
     * handler, el framework igual resolvería a UNKNOWN vía Status.fromThrowable(...);
     * se deja explícito como INTERNAL para no filtrar detalles internos del servidor,
     * en paralelo con el fallback HTTP 500 de GlobalExceptionHandler.
     */
    @GrpcExceptionHandler(DniAlreadyExistsException.class)
    public Status handleDniAlreadyExists(DniAlreadyExistsException ex) {
        return Status.ALREADY_EXISTS.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(EmailAlreadyExistsException.class)
    public Status handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return Status.ALREADY_EXISTS.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(DataIntegrityViolationException.class)
    public Status handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return Status.ALREADY_EXISTS.withDescription("El DNI o email ya se encuentra registrado.");
    }

    @GrpcExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Status handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return Status.PERMISSION_DENIED.withDescription("Acceso denegado: se requieren permisos de ADMINISTRADOR");
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleGeneric(Exception ex) {
        return Status.INTERNAL.withDescription("Ha ocurrido un error inesperado en el servidor");
    }
}
