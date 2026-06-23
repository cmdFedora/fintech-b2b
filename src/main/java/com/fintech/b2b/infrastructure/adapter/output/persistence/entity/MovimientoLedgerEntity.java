package com.fintech.b2b.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fintech.b2b.domain.model.TipoMovimiento;

@Entity
@Table(name = "movimientos_ledger")
public class MovimientoLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaccion_id", nullable = false)
    private Long transaccionId;

    @Column(name = "billetera_id", nullable = false)
    private Long billeteraId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 10)
    private TipoMovimiento tipoMovimiento;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "saldo_posterior", nullable = false)
    private BigDecimal saldoPosterior; // Foto de auditoría

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }

    // Constructor vacío para JPA
    public MovimientoLedgerEntity() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTransaccionId() { return transaccionId; }
    public void setTransaccionId(Long transaccionId) { this.transaccionId = transaccionId; }
    public Long getBilleteraId() { return billeteraId; }
    public void setBilleteraId(Long billeteraId) { this.billeteraId = billeteraId; }
    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public BigDecimal getSaldoPosterior() { return saldoPosterior; }
    public void setSaldoPosterior(BigDecimal saldoPosterior) { this.saldoPosterior = saldoPosterior; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}