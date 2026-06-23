package com.fintech.b2b.infrastructure.adapter.input.rest.dto;

import java.time.LocalDateTime;

/**
 * Estructura canónica inmutable para todas las respuestas de error del sistema.
 * Cumple con el estándar RFC 7807 (Problem Details for HTTP APIs).
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}