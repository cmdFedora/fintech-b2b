package com.fintech.b2b.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.b2b.application.port.in.RealizarTransferenciaCommand;
import com.fintech.b2b.application.port.in.RealizarTransferenciaUseCase;
import com.fintech.b2b.domain.model.Billetera;
import com.fintech.b2b.domain.model.Transaccion;
import com.fintech.b2b.domain.model.MovimientoLedger; 
import com.fintech.b2b.domain.model.TipoMovimiento;
import com.fintech.b2b.domain.model.OutboxEvent;
import com.fintech.b2b.domain.model.exception.BilleteraNoEncontradaException;
import com.fintech.b2b.domain.model.exception.OperacionInvalidaException;
import com.fintech.b2b.domain.model.port.BilleteraRepositoryPort;
import com.fintech.b2b.domain.model.port.MovimientoLedgerRepositoryPort;
import com.fintech.b2b.domain.model.port.OutboxEventRepositoryPort;
import com.fintech.b2b.domain.model.port.TransaccionRepositoryPort;
import com.fintech.b2b.domain.model.port.UsuarioAutenticadoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class RealizarTransferenciaUseCaseImpl implements RealizarTransferenciaUseCase {

    private final BilleteraRepositoryPort billeteraRepositoryPort;
    private final TransaccionRepositoryPort transaccionRepositoryPort;
    private final MovimientoLedgerRepositoryPort movimientoLedgerRepositoryPort; 
    private final UsuarioAutenticadoPort usuarioAutenticadoPort;
    
    // Nuevas dependencias para el Outbox
    private final OutboxEventRepositoryPort outboxEventRepositoryPort;
    private final ObjectMapper objectMapper;

    public RealizarTransferenciaUseCaseImpl(BilleteraRepositoryPort billeteraRepositoryPort,
                                           TransaccionRepositoryPort transaccionRepositoryPort,
                                           MovimientoLedgerRepositoryPort movimientoLedgerRepositoryPort,
                                           UsuarioAutenticadoPort usuarioAutenticadoPort,
                                           OutboxEventRepositoryPort outboxEventRepositoryPort,
                                           ObjectMapper objectMapper) {
        this.billeteraRepositoryPort = billeteraRepositoryPort;
        this.transaccionRepositoryPort = transaccionRepositoryPort;
        this.movimientoLedgerRepositoryPort = movimientoLedgerRepositoryPort;
        this.usuarioAutenticadoPort = usuarioAutenticadoPort;
        this.outboxEventRepositoryPort = outboxEventRepositoryPort;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional // 🔥 Todo o Nada: Saldo, Ledger y OutboxEvent se guardan juntos
    public Transaccion ejecutar(RealizarTransferenciaCommand command) {
        
        Long usuarioOrigenId = usuarioAutenticadoPort.getIdUsuarioActual();

        Billetera billeteraOrigen = billeteraRepositoryPort.buscarPorUsuarioId(usuarioOrigenId)
                .orElseThrow(() -> new BilleteraNoEncontradaException("No se encontró una billetera activa para el usuario autenticado."));

        Billetera billeteraDestino = billeteraRepositoryPort.buscarPorId(command.billeteraDestinoId())
                .orElseThrow(() -> new BilleteraNoEncontradaException("La billetera de destino no existe en el sistema."));

        if (billeteraOrigen.getId().equals(billeteraDestino.getId())) {
            throw new OperacionInvalidaException("Operación inválida: No es posible realizar transferencias hacia la misma cuenta de origen.");
        }

        Transaccion transaccion = Transaccion.crear(
                billeteraOrigen.getId(),
                billeteraDestino.getId(),
                command.monto(),
                command.concepto(),
                command.idempotencyKey()
        );

        billeteraOrigen.debitar(command.monto());
        billeteraDestino.acreditar(command.monto());

        billeteraRepositoryPort.guardar(billeteraOrigen);
        billeteraRepositoryPort.guardar(billeteraDestino);

        transaccion.completar();

        Transaccion transaccionGuardada = transaccionRepositoryPort.guardar(transaccion);

        MovimientoLedger debito = MovimientoLedger.crear(
                transaccionGuardada.getId(),
                billeteraOrigen.getId(),
                TipoMovimiento.DEBITO,
                command.monto(),
                billeteraOrigen.getSaldo().monto() 
        );

        MovimientoLedger credito = MovimientoLedger.crear(
                transaccionGuardada.getId(),
                billeteraDestino.getId(),
                TipoMovimiento.CREDITO,
                command.monto(),
                billeteraDestino.getSaldo().monto() 
        );

        movimientoLedgerRepositoryPort.guardar(debito);
        movimientoLedgerRepositoryPort.guardar(credito);

        // 11. PATRÓN OUTBOX: Empacar y dejar en la bandeja de salida
        try {
            // Armamos un pequeño resumen en JSON con la info crítica para el mundo exterior
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "transaccionId", transaccionGuardada.getId(),
                    "origenId", billeteraOrigen.getId(),
                    "destinoId", billeteraDestino.getId(),
                    "monto", command.monto(),
                    "estado", transaccionGuardada.getEstado().name()
            ));

            OutboxEvent evento = OutboxEvent.crear(
                    "Transaccion", 
                    transaccionGuardada.getId().toString(), 
                    "TransferenciaCompletada", 
                    payloadJson
            );

            outboxEventRepositoryPort.guardar(evento);

        } catch (Exception e) {
            // Si estalla el conversor JSON, abortamos toda la transacción de base de datos
            throw new RuntimeException("Error fatal empacando el evento Outbox", e);
        }

        return transaccionGuardada;
    }
}