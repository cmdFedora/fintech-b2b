package com.fintech.b2b.application.service;

import com.fintech.b2b.application.port.in.ConsultarSaldoUseCase;
import com.fintech.b2b.domain.model.Billetera;
import com.fintech.b2b.domain.model.port.BilleteraRepositoryPort;
import com.fintech.b2b.domain.model.exception.BilleteraNoEncontradaException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true) // CLAVE CQRS: Optimiza la memoria porque no guarda cambios
public class ConsultarSaldoUseCaseImpl implements ConsultarSaldoUseCase {

    private final BilleteraRepositoryPort billeteraRepositoryPort;

    public ConsultarSaldoUseCaseImpl(BilleteraRepositoryPort billeteraRepositoryPort) {
        this.billeteraRepositoryPort = billeteraRepositoryPort;
    }

    @Override
    public BigDecimal ejecutar(Long usuarioId) {
        // 1. Buscamos la billetera usando el puerto ya creado
    	Billetera billetera = billeteraRepositoryPort.buscarPorUsuarioId(usuarioId)
                // Usamos la excepción de negocio aquí
                .orElseThrow(() -> new BilleteraNoEncontradaException("No se encontró una billetera para el usuario ID: " + usuarioId));

        return billetera.getSaldo().monto();
    }
}