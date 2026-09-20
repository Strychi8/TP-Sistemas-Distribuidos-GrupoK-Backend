package com.empresa_rentar.web_services.mapper;

import com.empresa_rentar.web_services.dto.response.ReservaResponseDTO;
import com.empresa_rentar.web_services.model.Reserva;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {
    public ReservaResponseDTO mapToResponseDTO(Reserva r){
        return new ReservaResponseDTO(
                r.getIdReserva(),
                r.getCliente().getIdCliente(),
                r.getCliente().getNombre() + " " + r.getCliente().getApellido(),
                r.getVehiculo().getIdVehiculo(),
                r.getVehiculo().getPatente(),
                r.getVehiculo().getMarca() + " " + r.getVehiculo().getModelo(),
                r.getFechaInicio(),
                r.getFechaFin(),
                r.getPrecioDiario(),
                r.getImporteTotal(),
                r.getEstado()
        );
    }
}
