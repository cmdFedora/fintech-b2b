package com.fintech.b2b.infrastructure.adapter.output.persistence;

import com.fintech.b2b.domain.model.EstadoTransaccion;
import com.fintech.b2b.domain.model.Transaccion;
import com.fintech.b2b.domain.model.TipoMovimiento;
import com.fintech.b2b.domain.model.port.TransaccionRepositoryPort;
import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.MovimientoLedgerEntity;
import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.TransaccionEntity;
import com.fintech.b2b.infrastructure.adapter.output.persistence.repository.MovimientoLedgerJpaRepository;
import com.fintech.b2b.infrastructure.adapter.output.persistence.repository.TransaccionJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransaccionRepositoryAdapter implements TransaccionRepositoryPort {

    private final TransaccionJpaRepository transaccionJpaRepository;
    private final MovimientoLedgerJpaRepository ledgerJpaRepository;

    public TransaccionRepositoryAdapter(TransaccionJpaRepository transaccionJpaRepository, 
                                        MovimientoLedgerJpaRepository ledgerJpaRepository) {
        this.transaccionJpaRepository = transaccionJpaRepository;
        this.ledgerJpaRepository = ledgerJpaRepository;
    }

    @Override
    public Optional<Transaccion> buscarPorCorrelationId(String correlationId) {
        return transaccionJpaRepository.findByCorrelationId(correlationId)
                .map(this::mapearADominio);
    }

    @Override
    public Transaccion guardar(Transaccion transaccion) {
        // 1. Guardar la cabecera de la transacción
        TransaccionEntity entity = mapearAEntity(transaccion);
        TransaccionEntity savedEntity = transaccionJpaRepository.save(entity);

        // 2. Generar el registro inmutable de Partida Doble (Ledger)
        // NOTA: Para respetar tu V1 estrictamente, registramos DÉBITO y CRÉDITO.
        // El 'saldoPosterior' lo enviaremos como cero temporalmente o nulo hasta que
        // el Dominio o el Use Case nos pase el saldo exacto post-operación si se requiere auditoría estricta.
        
        MovimientoLedgerEntity debito = new MovimientoLedgerEntity();
        debito.setTransaccionId(savedEntity.getId());
        debito.setBilleteraId(transaccion.getBilleteraOrigenId());
        debito.setTipoMovimiento(TipoMovimiento.DEBITO);
        debito.setMonto(transaccion.getMonto());
        debito.setSaldoPosterior(BigDecimal.ZERO); // Auditoría de saldo snapshot

        MovimientoLedgerEntity credito = new MovimientoLedgerEntity();
        credito.setTransaccionId(savedEntity.getId());
        credito.setBilleteraId(transaccion.getBilleteraDestinoId());
        credito.setTipoMovimiento(TipoMovimiento.CREDITO);
        credito.setMonto(transaccion.getMonto());
        credito.setSaldoPosterior(BigDecimal.ZERO); // Auditoría de saldo snapshot

        ledgerJpaRepository.save(debito);
        ledgerJpaRepository.save(credito);

        return mapearADominio(savedEntity, transaccion.getBilleteraOrigenId(), transaccion.getBilleteraDestinoId());
    }

    // --- Mapeadores ---

    private TransaccionEntity mapearAEntity(Transaccion transaccion) {
        TransaccionEntity entity = new TransaccionEntity();
        // Mapeamos idempotencyKey del dominio al correlationId de la base de datos (V1)
        entity.setCorrelationId(transaccion.getIdempotencyKey().toString());
        entity.setMontoTotal(transaccion.getMonto());
        entity.setConcepto(transaccion.getConcepto());
        entity.setEstado(transaccion.getEstado());
        return entity;
    }

    private Transaccion mapearADominio(TransaccionEntity entity) {
        // Utilizado para la validación de idempotencia donde origen y destino no son relevantes
        return mapearADominio(entity, null, null);
    }

    private Transaccion mapearADominio(TransaccionEntity entity, Long origenId, Long destinoId) {
        // Reconstruimos la entidad del dominio mediante reflexión o a través de un constructor ajustado.
        // Dado que nuestro Factory Method "crear()" inicia en PENDING y genera fechas nuevas, 
        // aquí instanciamos de forma transparente los datos recuperados de BD.
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