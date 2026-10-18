package com.rentar.rental_service.exception;

import com.rentar.rental_service.exception.custom.BadRequestException;
import com.rentar.rental_service.exception.custom.BusinessException;
import com.rentar.rental_service.exception.custom.ConflictException;
import com.rentar.rental_service.exception.custom.ResourceNotFoundException;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import org.springframework.grpc.server.advice.GrpcAdvice;
import org.springframework.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(ResourceNotFoundException.class)
    public Status handleNotFound(ResourceNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(BadRequestException.class)
    public Status handleBadRequest(BadRequestException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(ConflictException.class)
    public Status handleConflict(ConflictException ex) {
        return Status.ALREADY_EXISTS.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(BusinessException.class)
    public Status handleBusiness(BusinessException ex) {
        return Status.FAILED_PRECONDITION.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public Status handleIllegalArgument(IllegalArgumentException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
    }

    // Si el servicio lanza un Status* deliberado, se respeta (evita que el catch-all lo pise).
    @GrpcExceptionHandler(StatusRuntimeException.class)
    public Status handleStatusRuntime(StatusRuntimeException ex) {
        return ex.getStatus();
    }

    @GrpcExceptionHandler(StatusException.class)
    public Status handleStatus(StatusException ex) {
        return ex.getStatus();
    }

    @GrpcExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Status handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return Status.PERMISSION_DENIED.withDescription("Acceso denegado: se requieren permisos de ADMINISTRADOR");
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleUnexpected(Exception ex) {
        return Status.INTERNAL.withDescription("Error interno del servicio - RentalService gRPC");
    }
}
