-- V2__add_financial_constraints.sql

-- 1. Restricción Física contra el Doble Gasto (Línea de defensa 1)
-- Utilizamos 'saldo_actual' tal como está definido en tu V1.
ALTER TABLE billeteras 
ADD CONSTRAINT chk_billetera_saldo_positivo CHECK (saldo_actual >= 0);

-- 2. Soporte para el Concepto de la Transferencia
-- Añadimos la columna concepto a tu tabla transacciones para que coincida con nuestro Dominio.
-- Tu V1 ya tiene 'estado' y usaremos 'correlation_id' como nuestra llave de idempotencia.
ALTER TABLE transacciones 
ADD COLUMN concepto VARCHAR(255);