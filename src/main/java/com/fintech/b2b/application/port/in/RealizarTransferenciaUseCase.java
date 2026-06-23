package com.fintech.b2b.application.port.in;

import com.fintech.b2b.domain.model.Transaccion;

/**
 * Puerto de entrada (Driving Port) que define la operación de transferencia entre billeteras.
 */
public interface RealizarTransferenciaUseCase {
    
    /**
     * Ejecuta el flujo transaccional orquestando los puertos de persistencia y seguridad.
     *
     * @param command Contenedor inmutable de los parámetros de la transferencia.
     * @return La transacción completada y registrada en el Ledger.
     */
    Transaccion ejecutar(RealizarTransferenciaCommand command);
}