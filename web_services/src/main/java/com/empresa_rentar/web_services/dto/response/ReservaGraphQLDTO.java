package com.empresa_rentar.web_services.dto.response;
import com.empresa_rentar.web_services.enums.EstadoReserva;

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
    private ReservaClienteDTO cliente;
    private VehiculoResponseDTO vehiculo;
    private String fechaInicio;
    private String fechaFin;
    private BigDecimal importeTotal;
    private BigDecimal precioDiario;
    private EstadoReserva estado;
}