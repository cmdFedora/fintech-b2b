package com.fintech.b2b.domain.model.port;

import com.fintech.b2b.domain.model.OutboxEvent;
import java.util.List;

public interface OutboxEventRepositoryPort {
    OutboxEvent guardar(OutboxEvent event);
    List<OutboxEvent> buscarPendientes(int limite);
}