package com.rentar.vehicle_service.service;

import com.rentar.vehicle_service.dto.request.VehiculoExternoRequestDTO;
import com.rentar.vehicle_service.dto.response.VehiculoExternoResponseDTO;
import com.rentar.vehicle_service.kafka.event.ValidacionPatenteEvent;
import com.rentar.vehicle_service.kafka.event.ValidacionTitularidadEvent;
import com.rentar.vehicle_service.kafka.event.VehiculoExternoCompletadaEvent;

import java.util.List;

public interface IVehiculoExternoService {
    VehiculoExternoResponseDTO registrarVehiculoExterno(VehiculoExternoRequestDTO dto);
    List<VehiculoExternoResponseDTO> listarVehiculosExternosPorCliente(Long clienteId);
    void procesarValidacionPatente(ValidacionPatenteEvent event);
    void procesarValidacionTitularidad(ValidacionTitularidadEvent event);
    void procesarCompletada(VehiculoExternoCompletadaEvent event);
}
