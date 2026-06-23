package com.fintech.b2b.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fintech.b2b.domain.model.exception.SaldoInvalidoException;

public class Transaccion {
    private Long id;
    private Long billeteraOrigenId;
    private Long billeteraDestinoId;
    private BigDecimal monto;
    private String concepto;
    private LocalDateTime fecha;
    
    // Identificador único generado por el cliente para evitar cobros dobles
    private UUID idempotencyKey; 
    
    private EstadoTransaccion estado;

    // Constructor privado para forzar el uso del Factory Method
    private Transaccion(Long id, Long billeteraOrigenId, Long billeteraDestinoId, BigDecimal monto, 
                        String concepto, LocalDateTime fecha, UUID idempotencyKey, EstadoTransaccion estado) {
        this.id = id;
        this.billeteraOrigenId = billeteraOrigenId;
        this.billeteraDestinoId = billeteraDestinoId;
        this.monto = monto;
        this.concepto = concepto;
        this.fecha = fecha;
        this.idempotencyKey = idempotencyKey;
        this.estado = estado;
    }

    /**
     * Factory Method principal para iniciar una transacción.
     */
    public static Transaccion crear(Long billeteraOrigenId, Long billeteraDestinoId, 
                                    BigDecimal monto, String concepto, UUID idempotencyKey) {
    	
    	if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SaldoInvalidoException("El monto de la transacción debe ser mayor a cero.");
        }
        if (concepto == null || concepto.trim().isEmpty()) {
            throw new IllegalArgumentException("El concepto es obligatorio."); // Aquí sí cabe porque es un error de formato de entrada
        }
    	
        return new Transaccion(
                null, 
                billeteraOrigenId, 
                billeteraDestinoId, 
                monto, 
                concepto, 
                LocalDateTime.now(), 
                idempotencyKey,
                EstadoTransaccion.PENDING // Nace pendiente
        );
    }

    public void completar() {
        this.estado = EstadoTransaccion.COMPLETED;
    }
    
    public void marcarComoFallida() {
        this.estado = EstadoTransaccion.FAILED;
    }

    // Getters
    public Long getId() { return id; }
    public Long getBilleteraOrigenId() { return billeteraOrigenId; }
    public Long getBilleteraDestinoId() { return billeteraDestinoId; }
    public BigDecimal getMonto() { return monto; }
    public String getConcepto() { return concepto; }
    public LocalDateTime getFecha() { return fecha; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public EstadoTransaccion getEstado() { return estado; }
}