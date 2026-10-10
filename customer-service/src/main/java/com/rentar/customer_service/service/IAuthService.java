package com.rentar.customer_service.service;


import com.rentar.customer_service.dto.request.LoginRequestDTO;
import com.rentar.customer_service.dto.response.AuthResponseDTO;
import com.rentar.customer_service.model.Usuario;

public interface IAuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    void logout(String token);
    Usuario getCurrentUser();
}
