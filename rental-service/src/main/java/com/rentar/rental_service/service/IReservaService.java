package com.rentar.rental_service.service;

import com.rentar.rental_service.dto.request.FiltroReservaDTO;
import com.rentar.rental_service.dto.request.ReservaRequestDTO;
import com.rentar.rental_service.dto.response.ReservaGraphQLDTO;
import com.rentar.rental_service.dto.response.ReservaResponseDTO;

import java.util.List;

public interface IReservaService {
    ReservaResponseDTO crearReserva(ReservaRequestDTO dto);
    ReservaResponseDTO cancelarReserva(Long idReserva);
    ReservaResponseDTO obtenerPorId(Long idReserva);
    List<ReservaGraphQLDTO> consultarHistorial(Long idCliente);
    List<ReservaGraphQLDTO> consultarReservas(FiltroReservaDTO filtro);
}
