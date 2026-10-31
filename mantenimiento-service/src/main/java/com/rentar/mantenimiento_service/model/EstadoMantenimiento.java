package com.rentar.mantenimiento_service.model;

/**
 * Estados posibles del ciclo de vida de un mantenimiento preventivo.
 *
 * <p>Una solicitud comienza pendiente y luego registra si el mantenimiento
 * pudo realizarse o no.</p>
 */
public enum EstadoMantenimiento {
    PENDIENTE,
    REALIZADO,
    NO_REALIZADO
}
