package com.fintech.b2b.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;
/**
 * Objeto inmutable que transporta la intención de realizar una transferencia.
 * Se valida a sí mismo en el momento de su creación (Fail-Fast).
 */
public record RealizarTransferenciaCommand(
        Long billeteraDestinoId,
        BigDecimal monto,
        String concepto,
        UUID idempotencyKey
) {
    public RealizarTransferenciaCommand {
        if (billeteraDestinoId == null) {
            throw new IllegalArgumentException("El ID de la billetera destino es obligatorio.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a transferir debe ser mayor a cero.");
        }
        if (concepto == null || concepto.trim().isEmpty()) {
            throw new IllegalArgumentException("El concepto de la transferencia es obligatorio.");
        }
        if (idempotencyKey == null) {
            throw new IllegalArgumentException("La llave de idempotencia (UUID) es obligatoria para prevenir cobros dobles.");
        }
    }
}