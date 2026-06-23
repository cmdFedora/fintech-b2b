package com.fintech.b2b.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Billetera {
    private Long id;
    private Long usuarioId;
    private Saldo saldo;
    private Long version; 

    public Billetera(Long id, Long usuarioId, BigDecimal montoInicial, Long version) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId, "El usuarioId no puede ser nulo");
        Objects.requireNonNull(montoInicial, "El monto inicial no puede ser nulo");
        
        this.saldo = new Saldo(montoInicial);
        // Inicialización segura para Optimistic Locking
        this.version = version != null ? version : 0L; 
    }

    public void debitar(BigDecimal monto) {
        this.saldo = this.saldo.restar(monto);
    }

    public void acreditar(BigDecimal monto) {
        this.saldo = this.saldo.sumar(monto);
    }

    public boolean esPropietario(Long usuarioId) {
        return Objects.equals(this.usuarioId, usuarioId);
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public Saldo getSaldo() { return saldo; }
    public Long getVersion() { return version; }
}