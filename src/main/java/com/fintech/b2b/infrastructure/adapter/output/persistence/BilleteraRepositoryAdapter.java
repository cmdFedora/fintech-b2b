package com.fintech.b2b.infrastructure.adapter.output.persistence;

import com.fintech.b2b.domain.model.Billetera;
import com.fintech.b2b.domain.model.EstadoBilletera;
import com.fintech.b2b.domain.model.exception.ConcurrenciaException;
import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.BilleteraEntity;
import com.fintech.b2b.infrastructure.adapter.output.persistence.repository.BilleteraJpaRepository;
import com.fintech.b2b.domain.model.port.BilleteraRepositoryPort; 

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BilleteraRepositoryAdapter implements BilleteraRepositoryPort {

    private final BilleteraJpaRepository jpaRepository;

    public BilleteraRepositoryAdapter(BilleteraJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Billetera> buscarPorId(Long id) {
        return jpaRepository.findById(id)
                .map(this::mapearADominio);
    }

    @Override
    public Optional<Billetera> buscarPorUsuarioId(Long usuarioId) {
        // Aprovechamos el método derivado de Spring Data JPA que implementaremos en el JpaRepository
        return jpaRepository.findByUsuarioId(usuarioId)
                .map(this::mapearADominio);
    }

    @Override
    public Billetera guardar(Billetera billetera) {
        try {
            BilleteraEntity entity = mapearAEntity(billetera);
            BilleteraEntity savedEntity = jpaRepository.save(entity);
            
            // Forzamos la escritura inmediata en PostgreSQL para activar el @Version
            jpaRepository.flush(); 
            
            // Devolvemos el modelo de dominio con la versión actualizada por la BD
            return mapearADominio(savedEntity);
            
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConcurrenciaException("La billetera fue modificada por otra transacción masiva. Por favor, intente nuevamente.");
        }
    }

    // --- Traductores Hexagonales (Mapeadores) ---

    private Billetera mapearADominio(BilleteraEntity entity) {
        return new Billetera(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getSaldoActual(),
                entity.getVersion()
        );
    }

    private BilleteraEntity mapearAEntity(Billetera billetera) {
        return new BilleteraEntity(
                billetera.getId(),
                billetera.getUsuarioId(),
                billetera.getSaldo().monto(), 
                EstadoBilletera.ACTIVA, 
                billetera.getVersion()
        );
    }
}