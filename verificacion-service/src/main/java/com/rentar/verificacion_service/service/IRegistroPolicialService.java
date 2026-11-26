package com.rentar.verificacion_service.service;

import com.rentar.verificacion_service.model.PatenteRobada;

import java.util.List;

/** Define las operaciones del registro policial simulado de patentes. */
public interface IRegistroPolicialService {
    /** Comprueba si la patente figura como robada. */
    boolean esRobado(String patente);

    /** Registra una patente denunciada y el motivo asociado. */
    PatenteRobada registrarPatenteRobada(String patente, String motivo);

    /** Lista las patentes registradas como robadas. */
    List<PatenteRobada> listarPatentesRobadas();
}
