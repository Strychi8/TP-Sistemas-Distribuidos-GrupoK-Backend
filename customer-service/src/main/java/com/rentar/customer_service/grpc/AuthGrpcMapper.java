package com.rentar.customer_service.grpc;

import com.rentar.customer.grpc.LoginRequest;
import com.rentar.customer.grpc.LoginResponse;
import com.rentar.customer.grpc.LogoutResponse;
import com.rentar.customer.grpc.VerificarTokenResponse;
import com.rentar.customer_service.dto.request.LoginRequestDTO;
import com.rentar.customer_service.dto.response.AuthResponseDTO;

/**
 * Traduce los DTOs de autenticación (Hito 1, IAuthService/JwtService) a los
 * mensajes protobuf de H2-4 (Login, Logout, VerificarTokenBlacklist).
 * Mapper puro: sin lógica de negocio ni acceso a repositorios.
 */
public class AuthGrpcMapper {

    private AuthGrpcMapper() {
    }

    public static LoginRequestDTO toLoginRequestDTO(LoginRequest request) {
        return LoginRequestDTO.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
    }

    public static LoginResponse toLoginResponse(AuthResponseDTO authResponse) {
        return LoginResponse.newBuilder()
                .setToken(authResponse.getToken())
                .build();
    }

    public static LogoutResponse toLogoutResponse(boolean success) {
        return LogoutResponse.newBuilder()
                .setSuccess(success)
                .build();
    }

    public static VerificarTokenResponse toVerificarTokenResponse(boolean revocado) {
        return VerificarTokenResponse.newBuilder()
                .setRevocado(revocado)
                .build();
    }
}
