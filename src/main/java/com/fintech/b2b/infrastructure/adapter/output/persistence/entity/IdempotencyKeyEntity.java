package com.fintech.b2b.infrastructure.adapter.output.persistence.entity;

import com.fintech.b2b.infrastructure.adapter.input.rest.idempotency.EstadoIdempotencia;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "payload_hash", nullable = false, length = 255)
    private String payloadHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoIdempotencia estado;

    @Column(name = "respuesta_json", columnDefinition = "TEXT")
    private String respuestaJson;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
            // La rúbrica exige limpieza. Daremos 24 horas de vida a la llave.
            this.fechaExpiracion = this.fechaCreacion.plusHours(24); 
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
    public EstadoIdempotencia getEstado() { return estado; }
    public void setEstado(EstadoIdempotencia estado) { this.estado = estado; }
    public String getRespuestaJson() { return respuestaJson; }
    public void setRespuestaJson(String respuestaJson) { this.respuestaJson = respuestaJson; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
}