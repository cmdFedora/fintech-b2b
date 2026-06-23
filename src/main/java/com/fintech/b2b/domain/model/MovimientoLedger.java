package com.fintech.b2b.domain.model;

import java.math.BigDecimal;

public class MovimientoLedger {
    
    private final Long id;
    private final Long transaccionId;
    private final Long billeteraId;
    private final TipoMovimiento tipoMovimiento;
    private final BigDecimal monto;
    private final BigDecimal saldoPosterior; // Clave para la auditoría financiera

    public MovimientoLedger(Long id, Long transaccionId, Long billeteraId, TipoMovimiento tipoMovimiento, BigDecimal monto, BigDecimal saldoPosterior) {
        this.id = id;
        this.transaccionId = transaccionId;
        this.billeteraId = billeteraId;
        this.tipoMovimiento = tipoMovimiento;
        this.monto = monto;
        this.saldoPosterior = saldoPosterior;
    }

    // Factory method para nuevos movimientos
    public static MovimientoLedger crear(Long transaccionId, Long billeteraId, TipoMovimiento tipoMovimiento, BigDecimal monto, BigDecimal saldoPosterior) {
        return new MovimientoLedger(null, transaccionId, billeteraId, tipoMovimiento, monto, saldoPosterior);
    }

    public Long getId() { return id; }
    public Long getTransaccionId() { return transaccionId; }
    public Long getBilleteraId() { return billeteraId; }
    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public BigDecimal getMonto() { return monto; }
    public BigDecimal getSaldoPosterior() { return saldoPosterior; }
}