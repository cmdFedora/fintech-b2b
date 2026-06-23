package com.fintech.b2b.infrastructure.adapter.input.rest.idempotency;

public enum EstadoIdempotencia {
    PROCESSING,
    COMPLETED,
    FAILED,
    EXPIRED
}
