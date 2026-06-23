package com.fintech.b2b.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO de entrada para la petición de transferencia.
 * Validado en la capa HTTP antes de llegar al dominio.
 */
public record TransferenciaRequest(
        
        @NotNull(message = "El ID de la billetera destino es obligatorio.")
        Long billeteraDestinoId,

        @NotNull(message = "El monto es obligatorio.")
        @Positive(message = "El monto debe ser un valor positivo mayor a cero.")
        BigDecimal monto,

        @NotBlank(message = "El concepto de la transferencia no puede estar vacío.")
        String concepto
) {}