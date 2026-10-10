package com.rentar.rental_service.dto.response;

import com.rentar.rental_service.enums.EstadoReserva;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaGraphQLDTO {
    private Long idReserva;
    private Long idCliente;
    private Long  idVehiculo;
    private String fechaInicio;
    private String fechaFin;
    private BigDecimal importeTotal;
    private BigDecimal precioDiario;
    private EstadoReserva estado;
}