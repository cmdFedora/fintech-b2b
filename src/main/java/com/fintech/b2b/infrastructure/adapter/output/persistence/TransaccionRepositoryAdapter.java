package com.fintech.b2b.infrastructure.adapter.output.persistence;

import com.fintech.b2b.domain.model.EstadoTransaccion;
import com.fintech.b2b.domain.model.Transaccion;
import com.fintech.b2b.domain.model.port.TransaccionRepositoryPort;
import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.TransaccionEntity;
import com.fintech.b2b.infrastructure.adapter.output.persistence.repository.TransaccionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TransaccionRepositoryAdapter implements TransaccionRepositoryPort {

    private final TransaccionJpaRepository transaccionJpaRepository;

    public TransaccionRepositoryAdapter(TransaccionJpaRepository transaccionJpaRepository) {
        this.transaccionJpaRepository = transaccionJpaRepository;
    }

    @Override
    public Optional<Transaccion> buscarPorCorrelationId(String correlationId) {
        return transaccionJpaRepository.findByCorrelationId(correlationId)
                .map(this::mapearADominio);
    }

    @Override
    public Transaccion guardar(Transaccion transaccion) {
        TransaccionEntity entity = mapearAEntity(transaccion);
        TransaccionEntity savedEntity = transaccionJpaRepository.save(entity);
        return mapearADominio(savedEntity, transaccion.getBilleteraOrigenId(), transaccion.getBilleteraDestinoId());
    }

    private TransaccionEntity mapearAEntity(Transaccion transaccion) {
        TransaccionEntity entity = new TransaccionEntity();
        entity.setCorrelationId(transaccion.getIdempotencyKey().toString());
        entity.setMontoTotal(transaccion.getMonto());
        entity.setConcepto(transaccion.getConcepto());
        entity.setEstado(transaccion.getEstado());
        return entity;
    }

    private Transaccion mapearADominio(TransaccionEntity entity) {
        return mapearADominio(entity, null, null);
    }

    private Transaccion mapearADominio(TransaccionEntity entity, Long origenId, Long destinoId) {
        Transaccion transaccion = Transaccion.crear(
                origenId, 
                destinoId, 
                entity.getMontoTotal(), 
                entity.getConcepto(), 
                UUID.fromString(entity.getCorrelationId())
        );
        
        if (entity.getEstado() == EstadoTransaccion.COMPLETED) {
            transaccion.completar();
        } else if (entity.getEstado() == EstadoTransaccion.FAILED) {
            transaccion.marcarComoFallida();
        }
        return transaccion;
    }
}