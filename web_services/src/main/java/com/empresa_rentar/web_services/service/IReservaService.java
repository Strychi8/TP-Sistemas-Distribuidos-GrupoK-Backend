package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.dto.request.FiltroReservaDTO;
import com.empresa_rentar.web_services.dto.request.ReservaRequestDTO;
import com.empresa_rentar.web_services.dto.response.ReservaResponseDTO;
import com.empresa_rentar.web_services.dto.response.ReservaGraphQLDTO;
import java.util.List;

public interface IReservaService {
    ReservaResponseDTO crearReserva(ReservaRequestDTO dto);
    ReservaResponseDTO cancelarReserva(Long idReserva);
    ReservaResponseDTO obtenerPorId(Long idReserva);
    List<ReservaGraphQLDTO> consultarReservas(FiltroReservaDTO filtro);
}
