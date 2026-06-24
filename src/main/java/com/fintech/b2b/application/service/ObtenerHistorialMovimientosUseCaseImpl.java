package com.fintech.b2b.application.service;

import com.fintech.b2b.application.port.in.ObtenerHistorialMovimientosUseCase;
import com.fintech.b2b.domain.model.Billetera;
import com.fintech.b2b.domain.model.MovimientoLedger;
import com.fintech.b2b.domain.model.exception.BilleteraNoEncontradaException;
import com.fintech.b2b.domain.model.port.BilleteraRepositoryPort;
import com.fintech.b2b.domain.model.port.MovimientoLedgerRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // 🔥 Clave para optimizar lectura
public class ObtenerHistorialMovimientosUseCaseImpl implements ObtenerHistorialMovimientosUseCase {

    private final BilleteraRepositoryPort billeteraRepositoryPort;
    private final MovimientoLedgerRepositoryPort movimientoLedgerRepositoryPort;

    public ObtenerHistorialMovimientosUseCaseImpl(
            BilleteraRepositoryPort billeteraRepositoryPort,
            MovimientoLedgerRepositoryPort movimientoLedgerRepositoryPort) {
        this.billeteraRepositoryPort = billeteraRepositoryPort;
        this.movimientoLedgerRepositoryPort = movimientoLedgerRepositoryPort;
    }

    @Override
    public Page<MovimientoLedger> ejecutar(Long usuarioId, Pageable pageable) {
        // 1. Validamos que el usuario tenga billetera
        Billetera billetera = billeteraRepositoryPort.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new BilleteraNoEncontradaException("No se encontró una billetera para el usuario ID: " + usuarioId));

        // 2. Buscamos el historial paginado
        return movimientoLedgerRepositoryPort.obtenerPorBilleteraId(billetera.getId(), pageable);
    }
}