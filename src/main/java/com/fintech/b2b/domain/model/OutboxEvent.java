package com.fintech.b2b.domain.model;

import java.time.LocalDateTime;

public class OutboxEvent {
    
    private final Long id;
    private final String aggregateType;
    private final String aggregateId;
    private final String eventType;
    private final String payload;
    private EstadoOutbox estado;
    private final LocalDateTime fechaCreacion;

    public enum EstadoOutbox {
        PENDING, PROCESSED, FAILED
    }

    public OutboxEvent(Long id, String aggregateType, String aggregateId, String eventType, String payload, EstadoOutbox estado, LocalDateTime fechaCreacion) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    // Factory method para crear un evento nuevo listo para enviar
    public static OutboxEvent crear(String aggregateType, String aggregateId, String eventType, String payload) {
        return new OutboxEvent(null, aggregateType, aggregateId, eventType, payload, EstadoOutbox.PENDING, null);
    }

    public void marcarComoProcesado() {
        this.estado = EstadoOutbox.PROCESSED;
    }

    public void marcarComoFallido() {
        this.estado = EstadoOutbox.FAILED;
    }

    public Long getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public EstadoOutbox getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}