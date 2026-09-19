package com.empresa_rentar.web_services.service;

import com.empresa_rentar.web_services.dto.request.FiltroReservaDTO;
import com.empresa_rentar.web_services.dto.response.ReservaResponseDTO;

import java.util.List;

public interface IReservaService {
    List<ReservaResponseDTO> consultarReservas(FiltroReservaDTO filtro);
}
