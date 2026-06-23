package com.fintech.b2b.application.service;

import com.fintech.b2b.application.port.in.RealizarTransferenciaCommand;
import com.fintech.b2b.application.port.in.RealizarTransferenciaUseCase;
import com.fintech.b2b.domain.model.Billetera;
import com.fintech.b2b.domain.model.Transaccion;
import com.fintech.b2b.domain.model.MovimientoLedger; 
import com.fintech.b2b.domain.model.TipoMovimiento; 
import com.fintech.b2b.domain.model.exception.BilleteraNoEncontradaException;
import com.fintech.b2b.domain.model.exception.OperacionInvalidaException;
import com.fintech.b2b.domain.model.port.BilleteraRepositoryPort;
import com.fintech.b2b.domain.model.port.MovimientoLedgerRepositoryPort; 
import com.fintech.b2b.domain.model.port.TransaccionRepositoryPort;
import com.fintech.b2b.domain.model.port.UsuarioAutenticadoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RealizarTransferenciaUseCaseImpl implements RealizarTransferenciaUseCase {

    private final BilleteraRepositoryPort billeteraRepositoryPort;
    private final TransaccionRepositoryPort transaccionRepositoryPort;
    private final MovimientoLedgerRepositoryPort movimientoLedgerRepositoryPort; 
    private final UsuarioAutenticadoPort usuarioAutenticadoPort;

    // Inyectamos el nuevo puerto del Ledger
    public RealizarTransferenciaUseCaseImpl(BilleteraRepositoryPort billeteraRepositoryPort,
                                           TransaccionRepositoryPort transaccionRepositoryPort,
                                           MovimientoLedgerRepositoryPort movimientoLedgerRepositoryPort,
                                           UsuarioAutenticadoPort usuarioAutenticadoPort) {
        this.billeteraRepositoryPort = billeteraRepositoryPort;
        this.transaccionRepositoryPort = transaccionRepositoryPort;
        this.movimientoLedgerRepositoryPort = movimientoLedgerRepositoryPort;
        this.usuarioAutenticadoPort = usuarioAutenticadoPort;
    }

    @Override
    @Transactional // 🔥 Garantiza atomicidad absoluta: Si falla el Ledger, el saldo hace rollback automático.
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

        // 9. GUARDAR CABECERA (Necesitamos hacerlo primero para obtener el ID real de la transacción)
        Transaccion transaccionGuardada = transaccionRepositoryPort.guardar(transaccion);

        // 10. ASENTAR EN EL LEDGER INMUTABLE (La Regla de Partida Doble pura del Sprint 3)
        MovimientoLedger debito = MovimientoLedger.crear(
                transaccionGuardada.getId(),
                billeteraOrigen.getId(),
                TipoMovimiento.DEBITO,
                command.monto(),
                billeteraOrigen.getSaldo().monto() //el snapshot financiero exacto
        );

        MovimientoLedger credito = MovimientoLedger.crear(
                transaccionGuardada.getId(),
                billeteraDestino.getId(),
                TipoMovimiento.CREDITO,
                command.monto(),
                billeteraDestino.getSaldo().monto() //snapshot financiero exacto
        );

        movimientoLedgerRepositoryPort.guardar(debito);
        movimientoLedgerRepositoryPort.guardar(credito);

        return transaccionGuardada;
    }
}