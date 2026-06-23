package com.fintech.b2b.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.envers.Audited;

import com.fintech.b2b.domain.model.EstadoBilletera;

@Entity
@Table(name = "billeteras")
@Audited
public class BilleteraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "saldo_actual", nullable = false)
    private BigDecimal saldoActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoBilletera estado;

    //DEFENSA 2: Optimistic Locking
    @Version 
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoBilletera.ACTIVA;
        }
    }

    // Constructor vacío requerido por JPA
    public BilleteraEntity() {}

    // Constructor para mapear desde el Dominio
    public BilleteraEntity(Long id, Long usuarioId, BigDecimal saldoActual, EstadoBilletera estado, Long version) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.saldoActual = saldoActual;
        this.estado = estado;
        this.version = version;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public BigDecimal getSaldoActual() { return saldoActual; }
    public void setSaldoActual(BigDecimal saldoActual) { this.saldoActual = saldoActual; }
    public EstadoBilletera getEstado() { return estado; }
    public void setEstado(EstadoBilletera estado) { this.estado = estado; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}