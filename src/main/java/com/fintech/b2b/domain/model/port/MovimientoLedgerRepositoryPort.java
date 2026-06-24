package com.fintech.b2b.domain.model.port;

import com.fintech.b2b.domain.model.MovimientoLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovimientoLedgerRepositoryPort {
    
    // Lo que ya teníamos de nuestro motor de transferencias
    MovimientoLedger guardar(MovimientoLedger movimiento);

    // El método para consultar el historial paginado
    Page<MovimientoLedger> obtenerPorBilleteraId(Long billeteraId, Pageable pageable);
}