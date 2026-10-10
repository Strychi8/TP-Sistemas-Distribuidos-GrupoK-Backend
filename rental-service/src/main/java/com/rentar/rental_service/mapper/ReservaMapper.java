package com.rentar.rental_service.mapper;

import com.rentar.rental_service.dto.response.ReservaResponseDTO;
import com.rentar.rental_service.model.Reserva;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {
    public ReservaResponseDTO mapToResponseDTO(Reserva r){
        return new ReservaResponseDTO(
                r.getIdReserva(),
                r.getIdCliente(),
                r.getIdVehiculo(),
                r.getFechaInicio(),
                r.getFechaFin(),
                r.getPrecioDiario(),
                r.getImporteTotal(),
                r.getEstado()
        );
    }
}
