package com.fintech.b2b.domain.model.port;

import com.fintech.b2b.domain.model.Billetera;
import java.util.Optional;

public interface BilleteraRepositoryPort {
    Optional<Billetera> buscarPorId(Long id);
    Optional<Billetera> buscarPorUsuarioId(Long usuarioId);
    Billetera guardar(Billetera billetera);
}