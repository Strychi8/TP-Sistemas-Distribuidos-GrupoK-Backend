package com.rentar.verificacion_service.service;

import com.rentar.verificacion_service.model.TitularidadVehiculo;

import java.util.List;

/** Define las operaciones del registro simulado de titularidad vehicular. */
public interface IRegistroTitularidadService {
    /** Resultado de comparar la solicitud con los datos del registro. */
    record ResultadoTitularidad(boolean valido, String documento, String nombreTitular) {}
   
    /** Comprueba si el cliente o su documento coincide con el titular de la patente. */
    ResultadoTitularidad verificarTitularidad(Long clienteId, String documento, String patente);

    /** Registra una titularidad simulada para una patente. */
    TitularidadVehiculo registrarTitularidad(Long clienteId, String documento, String patente, String titular);

    /** Lista las titularidades disponibles en el registro. */
    List<TitularidadVehiculo> listarTitularidades();
}
