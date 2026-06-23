package com.fintech.b2b.domain.model.port;

public interface UsuarioAutenticadoPort {
    /**
     * Devuelve el ID del usuario que está ejecutando la petición actual,
     * extrayéndolo directamente del token en memoria RAM.
     */
    Long getIdUsuarioActual();
}