package com.fintech.b2b.infrastructure.adapter.output.persistence;

import com.fintech.b2b.domain.model.OutboxEvent;
import com.fintech.b2b.domain.model.port.OutboxEventRepositoryPort;
import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.OutboxEventEntity;
import com.fintech.b2b.infrastructure.adapter.output.persistence.repository.OutboxEventJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OutboxEventRepositoryAdapter implements OutboxEventRepositoryPort {

    private final OutboxEventJpaRepository jpaRepository;

    public OutboxEventRepositoryAdapter(OutboxEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OutboxEvent guardar(OutboxEvent event) {
        OutboxEventEntity entity = mapearAEntity(event);
        OutboxEventEntity savedEntity = jpaRepository.save(entity);
        return mapearADominio(savedEntity);
    }

    @Override
    public List<OutboxEvent> buscarPendientes(int limite) {
        // Buscamos solo los PENDING y le ponemos un límite para no colapsar la RAM
        return jpaRepository.findByEstadoOrderByFechaCreacionAsc(OutboxEvent.EstadoOutbox.PENDING, PageRequest.of(0, limite))
                .stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    // --- Mapeadores ---

    private OutboxEventEntity mapearAEntity(OutboxEvent dominio) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(dominio.getId());
        entity.setAggregateType(dominio.getAggregateType());
        entity.setAggregateId(dominio.getAggregateId());
        entity.setEventType(dominio.getEventType());
        entity.setPayload(dominio.getPayload());
        entity.setEstado(dominio.getEstado());
        
        // Si el dominio nos dice que ya se procesó, sellamos la fecha de procesamiento
        if (dominio.getEstado() == OutboxEvent.EstadoOutbox.PROCESSED && dominio.getId() != null) {
            entity.setFechaProcesamiento(LocalDateTime.now());
        }
        return entity;
    }

    private OutboxEvent mapearADominio(OutboxEventEntity entity) {
        return new OutboxEvent(
                entity.getId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getEstado(),
                entity.getFechaCreacion()
        );
    }
}