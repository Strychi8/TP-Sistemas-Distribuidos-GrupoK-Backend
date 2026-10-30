package com.rentar.verificacion_service.service;

import com.rentar.verificacion_service.model.TitularidadVehiculo;

import java.util.List;

public interface IRegistroTitularidadService {
    record ResultadoTitularidad(boolean valido, String documento, String nombreTitular) {}
    // Valida si un cliente (por clienteId y/o documento) es el titular registral de la patente indicada
    ResultadoTitularidad verificarTitularidad(Long clienteId, String documento, String patente);
    // Registra o actualiza una titularidad simulada
    TitularidadVehiculo registrarTitularidad(Long clienteId, String documento, String patente, String titular);
    // Lista todas las titularidades registradas
    List<TitularidadVehiculo> listarTitularidades();
}
