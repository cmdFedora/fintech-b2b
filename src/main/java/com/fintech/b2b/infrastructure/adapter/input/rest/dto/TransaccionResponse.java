package com.fintech.b2b.infrastructure.adapter.input.rest.dto;

import java.time.LocalDateTime;

/**
 * DTO de salida que representa el resultado de la transferencia.
 * Mantiene tipos de datos nativos para que el framework decida la serialización JSON.
 */
public record TransaccionResponse(
        Long transaccionId,
        String estado,
        LocalDateTime fecha,
        String mensaje
) {}