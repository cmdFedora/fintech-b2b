package com.fintech.b2b.domain.model.port;

import com.fintech.b2b.domain.model.Transaccion;
import java.util.Optional;

public interface TransaccionRepositoryPort {
    Transaccion guardar(Transaccion transaccion);
    Optional<Transaccion> buscarPorCorrelationId(String correlationId);
}