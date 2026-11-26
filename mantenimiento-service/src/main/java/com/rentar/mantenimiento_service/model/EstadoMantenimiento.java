package com.rentar.mantenimiento_service.model;

public enum EstadoMantenimiento {
    /** La solicitud todavía no tiene un resultado. */
    PENDIENTE,
    /** El mantenimiento se completó. */
    REALIZADO,
    /** El mantenimiento no se completó. */
    NO_REALIZADO
}
