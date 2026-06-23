-- V6__outbox_pattern.sql
-- Descripción: Tabla para implementar el Patrón Outbox transaccional.

CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL, -- Ej: 'Transaccion'
    aggregate_id VARCHAR(100) NOT NULL,   -- El ID de la transacción
    event_type VARCHAR(100) NOT NULL,     -- Ej: 'TransferenciaCompletada'
    payload TEXT NOT NULL,                -- El contenido del evento en formato JSON
    estado VARCHAR(20) NOT NULL,          -- PENDING, PROCESSED, FAILED
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_procesamiento TIMESTAMP
);

-- Índice para que el "Cartero" (@Scheduled) encuentre rápido los mensajes pendientes
CREATE INDEX idx_outbox_estado ON outbox_events(estado);