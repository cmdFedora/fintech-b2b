package com.fintech.b2b.infrastructure.adapter.output.persistence.repository;

import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.BilleteraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BilleteraJpaRepository extends JpaRepository<BilleteraEntity, Long> {
	Optional<BilleteraEntity> findByUsuarioId(Long usuarioId);
}