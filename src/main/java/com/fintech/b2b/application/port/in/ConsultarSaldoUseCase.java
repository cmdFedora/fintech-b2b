package com.fintech.b2b.application.port.in;

import java.math.BigDecimal;

public interface ConsultarSaldoUseCase {
    /**
     * Consulta el saldo actual disponible de la billetera de un usuario.
     */
    BigDecimal ejecutar(Long usuarioId);
}