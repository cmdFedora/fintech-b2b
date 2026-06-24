package com.fintech.b2b.application.port.in;

import com.fintech.b2b.domain.model.MovimientoLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ObtenerHistorialMovimientosUseCase {
    /**
     * Obtiene el historial de movimientos de forma paginada para un usuario.
     */
    Page<MovimientoLedger> ejecutar(Long usuarioId, Pageable pageable);
}