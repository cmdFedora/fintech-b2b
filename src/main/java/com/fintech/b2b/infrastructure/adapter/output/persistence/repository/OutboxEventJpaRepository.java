package com.fintech.b2b.infrastructure.adapter.output.persistence.repository;

import com.fintech.b2b.domain.model.OutboxEvent.EstadoOutbox;
import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.OutboxEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {
    
    // Encuentra eventos por estado y usa Pageable para limitar la cantidad a leer por ciclo (ej. de 10 en 10)
    List<OutboxEventEntity> findByEstadoOrderByFechaCreacionAsc(EstadoOutbox estado, Pageable pageable);
    
}