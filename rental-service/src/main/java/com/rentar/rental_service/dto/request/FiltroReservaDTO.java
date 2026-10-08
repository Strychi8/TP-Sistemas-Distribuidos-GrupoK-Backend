package com.rentar.rental_service.dto.request;


import com.rentar.rental_service.enums.EstadoReserva;
import lombok.Data;

@Data
public class FiltroReservaDTO {
    private Long idCliente;
    private Long idVehiculo;
    private EstadoReserva estado;
    private String fechaDesde;
    private String fechaHasta;
}
