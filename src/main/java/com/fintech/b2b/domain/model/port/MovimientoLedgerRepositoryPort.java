package com.fintech.b2b.domain.model.port;

import com.fintech.b2b.domain.model.MovimientoLedger;

public interface MovimientoLedgerRepositoryPort {
    MovimientoLedger guardar(MovimientoLedger movimiento);
}