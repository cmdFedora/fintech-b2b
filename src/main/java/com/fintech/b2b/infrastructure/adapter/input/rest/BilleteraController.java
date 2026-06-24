package com.fintech.b2b.infrastructure.adapter.input.rest;

import com.fintech.b2b.application.port.in.ConsultarSaldoUseCase;
import com.fintech.b2b.application.port.in.ObtenerHistorialMovimientosUseCase;
import com.fintech.b2b.domain.model.MovimientoLedger;
import com.fintech.b2b.infrastructure.adapter.input.rest.dto.SaldoResponse;
import com.fintech.b2b.domain.model.UserIdentity; 

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/billeteras")
public class BilleteraController {

    private final ConsultarSaldoUseCase consultarSaldoUseCase;
    private final ObtenerHistorialMovimientosUseCase historialMovimientosUseCase;

    public BilleteraController(
            ConsultarSaldoUseCase consultarSaldoUseCase,
            ObtenerHistorialMovimientosUseCase historialMovimientosUseCase) {
        this.consultarSaldoUseCase = consultarSaldoUseCase;
        this.historialMovimientosUseCase = historialMovimientosUseCase;
    }

    @GetMapping("/mi-saldo")
    public ResponseEntity<SaldoResponse> obtenerMiSaldo(
            @AuthenticationPrincipal UserIdentity userIdentity) { // Spring inyecta mágicamente tu record aquí
        
        // Ejecutamos el caso de uso sacando el id() del record
        BigDecimal saldo = consultarSaldoUseCase.ejecutar(userIdentity.id());
        return ResponseEntity.ok(new SaldoResponse(saldo));
    }
    
    @GetMapping("/historial")
    public ResponseEntity<Page<MovimientoLedger>> obtenerMiHistorial(
            @AuthenticationPrincipal UserIdentity userIdentity,
            // @PageableDefault configura cómo se comporta si el frontend no manda parámetros. 
            // Aquí le decimos: "Dame de a 20 registros, ordenados por ID descendente (los más nuevos primero)"
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        // Ejecutamos el caso de uso pasando el usuario autenticado y el objeto de paginación
        Page<MovimientoLedger> historial = historialMovimientosUseCase.ejecutar(userIdentity.id(), pageable);
        
        return ResponseEntity.ok(historial);
    }
}