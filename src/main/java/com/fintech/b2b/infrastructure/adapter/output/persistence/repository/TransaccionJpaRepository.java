package com.fintech.b2b.infrastructure.adapter.output.persistence.repository;

import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.TransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TransaccionJpaRepository extends JpaRepository<TransaccionEntity, Long> {
    // Necesario para verificar la idempotencia
    Optional<TransaccionEntity> findByCorrelationId(String correlationId);
}