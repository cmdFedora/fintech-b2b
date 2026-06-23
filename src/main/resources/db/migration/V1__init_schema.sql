-- V1__init_schema.sql
-- Descripción: Tablas base para Usuarios, Billeteras y la estructura central del Ledger.

-- 1. Tabla de Usuarios (Autenticación y Roles)
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL, -- Ej: ROLE_TESORERO, ROLE_EMPLEADO
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla de Billeteras (Wallet)
CREATE TABLE billeteras (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    saldo_actual DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA', -- ACTIVA, BLOQUEADA
    version BIGINT NOT NULL DEFAULT 0, -- CRÍTICO: Usado por JPA (@Version) para el Bloqueo Optimista
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_billetera_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 3. Tabla de Control de Idempotencia (Prevención de dobles cobros)
CREATE TABLE idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    payload_hash VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL, -- PROCESSING, COMPLETED, FAILED, EXPIRED
    respuesta_json TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP NOT NULL,
    CONSTRAINT uk_client_idempotency UNIQUE (client_id, idempotency_key)
);

-- 4. El Ledger Financiero (Inmutable - Append Only)
-- Registra el evento general de la transacción
CREATE TABLE transacciones (
    id BIGSERIAL PRIMARY KEY,
    correlation_id VARCHAR(100) UNIQUE NOT NULL,
    idempotency_key_id BIGINT, -- Relación opcional con la llave que originó esto
    monto_total DECIMAL(15, 2) NOT NULL,
    estado VARCHAR(20) NOT NULL, -- APROBADA, RECHAZADA
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaccion_idempotency FOREIGN KEY (idempotency_key_id) REFERENCES idempotency_keys(id)
);

-- 5. Asientos Contables (Partida Doble)
-- Cada transferencia insertará dos registros aquí: un DÉBITO y un CRÉDITO. NUNCA se actualizan.
CREATE TABLE movimientos_ledger (
    id BIGSERIAL PRIMARY KEY,
    transaccion_id BIGINT NOT NULL,
    billetera_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(10) NOT NULL, -- DEBITO (resta), CREDITO (suma)
    monto DECIMAL(15, 2) NOT NULL,
    saldo_posterior DECIMAL(15, 2) NOT NULL, -- Foto estática del saldo en ese exacto momento para auditoría
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movimiento_transaccion FOREIGN KEY (transaccion_id) REFERENCES transacciones(id),
    CONSTRAINT fk_movimiento_billetera FOREIGN KEY (billetera_id) REFERENCES billeteras(id)
);