package com.rentar.verificacion_service.service;

import com.rentar.verificacion_service.model.PatenteRobada;

import java.util.List;

public interface IRegistroPolicialService {
    // Verifica si una patente se encuentra registrada como robada
    boolean esRobado(String patente);
    // Registra una nueva patente en el registro policial simulado
    PatenteRobada registrarPatenteRobada(String patente, String motivo);
    // Retorna todas las patentes robadas registradas
    List<PatenteRobada> listarPatentesRobadas();
}
