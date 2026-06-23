package com.fintech.b2b.infrastructure.adapter.output.persistence.repository;

import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.MovimientoLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoLedgerJpaRepository extends JpaRepository<MovimientoLedgerEntity, Long> {
}