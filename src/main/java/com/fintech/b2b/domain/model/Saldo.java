package com.fintech.b2b.domain.model;

import com.fintech.b2b.domain.model.exception.SaldoInsuficienteException;
import com.fintech.b2b.domain.model.exception.SaldoInvalidoException;
import java.math.BigDecimal;

public record Saldo(BigDecimal monto) {
    
    public Saldo {
        validarMontoBase(monto);
        // Normalización nativa: elimina la necesidad de sobrescribir equals() y hashCode()
        monto = monto.stripTrailingZeros(); 
    }

    private static void validarMontoBase(BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInvalidoException("El saldo no puede ser nulo o negativo.");
        }
    }

    private static void validarOperacion(BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SaldoInvalidoException("La cantidad a operar debe ser mayor a cero.");
        }
    }

    public Saldo sumar(BigDecimal cantidad) {
        validarOperacion(cantidad);
        return new Saldo(this.monto.add(cantidad));
    }

    public Saldo restar(BigDecimal cantidad) {
        validarOperacion(cantidad);
        if (this.monto.compareTo(cantidad) < 0) {
            throw new SaldoInsuficienteException("La billetera no cuenta con fondos suficientes.");
        }
        return new Saldo(this.monto.subtract(cantidad));
    }
}