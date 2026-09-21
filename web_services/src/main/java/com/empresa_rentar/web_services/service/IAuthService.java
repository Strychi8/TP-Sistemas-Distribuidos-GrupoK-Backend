package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.dto.request.LoginRequestDTO;
import com.empresa_rentar.web_services.dto.response.AuthResponseDTO;
import com.empresa_rentar.web_services.model.Usuario;

public interface IAuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    void logout(String token);
    Usuario getCurrentUser();
}
