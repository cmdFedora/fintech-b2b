package com.fintech.b2b.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fintech.b2b.domain.model.EstadoTransaccion;

@Entity
@Table(name = "transacciones")
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlation_id", nullable = false, unique = true, length = 100)
    private String correlationId; // Esta es nuestra llave de idempotencia del Dominio

    @Column(name = "idempotency_key_id")
    private Long idempotencyKeyId;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoTransaccion estado;

    @Column(name = "concepto", length = 255)
    private String concepto; // Añadido en V2

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }

    // Constructor vacío para JPA
    public TransaccionEntity() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public Long getIdempotencyKeyId() { return idempotencyKeyId; }
    public void setIdempotencyKeyId(Long idempotencyKeyId) { this.idempotencyKeyId = idempotencyKeyId; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public EstadoTransaccion getEstado() { return estado; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}