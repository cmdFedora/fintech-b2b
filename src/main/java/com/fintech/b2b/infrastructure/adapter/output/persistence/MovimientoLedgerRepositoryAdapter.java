package com.fintech.b2b.infrastructure.adapter.output.persistence;

import com.fintech.b2b.domain.model.MovimientoLedger;
import com.fintech.b2b.domain.model.port.MovimientoLedgerRepositoryPort;
import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.MovimientoLedgerEntity;
import com.fintech.b2b.infrastructure.adapter.output.persistence.repository.MovimientoLedgerJpaRepository;
import org.springframework.stereotype.Component;

@Component // Esta anotación salva a Spring Boot del error de inyección
public class MovimientoLedgerRepositoryAdapter implements MovimientoLedgerRepositoryPort {

    private final MovimientoLedgerJpaRepository jpaRepository;

    public MovimientoLedgerRepositoryAdapter(MovimientoLedgerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MovimientoLedger guardar(MovimientoLedger movimiento) {
        MovimientoLedgerEntity entity = mapearAEntity(movimiento);
        MovimientoLedgerEntity savedEntity = jpaRepository.save(entity);
        return mapearADominio(savedEntity);
    }

    // --- Mapeadores ---

    private MovimientoLedgerEntity mapearAEntity(MovimientoLedger dominio) {
        MovimientoLedgerEntity entity = new MovimientoLedgerEntity();
        // Si el ID es null (nuevo registro), JPA le asignará uno automáticamente al guardar
        entity.setId(dominio.getId()); 
        entity.setTransaccionId(dominio.getTransaccionId());
        entity.setBilleteraId(dominio.getBilleteraId());
        entity.setTipoMovimiento(dominio.getTipoMovimiento());
        entity.setMonto(dominio.getMonto());
        entity.setSaldoPosterior(dominio.getSaldoPosterior());
        return entity;
    }

    private MovimientoLedger mapearADominio(MovimientoLedgerEntity entity) {
        return new MovimientoLedger(
                entity.getId(),
                entity.getTransaccionId(),
                entity.getBilleteraId(),
                entity.getTipoMovimiento(),
                entity.getMonto(),
                entity.getSaldoPosterior()
        );
    }
}