package com.empresa_rentar.web_services.dto.request;

import com.empresa_rentar.web_services.enums.EstadoReserva;
import com.empresa_rentar.web_services.enums.TipoVehiculo;
import lombok.Data;

@Data
public class FiltroReservaDTO {
    private Long idCliente;
    private Long idVehiculo;
    private TipoVehiculo tipoVehiculo;
    private EstadoReserva estado;
    private String fechaDesde;
    private String fechaHasta;
}
