package com.fintech.b2b.infrastructure.adapter.input.rest;

import com.fintech.b2b.application.port.in.RealizarTransferenciaCommand;
import com.fintech.b2b.application.port.in.RealizarTransferenciaUseCase;
import com.fintech.b2b.domain.model.Transaccion;
import com.fintech.b2b.infrastructure.adapter.input.rest.dto.TransaccionResponse;
import com.fintech.b2b.infrastructure.adapter.input.rest.dto.TransferenciaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transferencias")
public class TransferenciaController {

    private final RealizarTransferenciaUseCase realizarTransferenciaUseCase;

    public TransferenciaController(RealizarTransferenciaUseCase realizarTransferenciaUseCase) {
        this.realizarTransferenciaUseCase = realizarTransferenciaUseCase;
    }

    @PostMapping
    public ResponseEntity<TransaccionResponse> realizarTransferencia(
            @Valid @RequestBody TransferenciaRequest request,
            @RequestHeader("Idempotency-Key") UUID idempotencyKey) {

        // 1. Armar el comando. El origen se extrae de forma segura en el Use Case vía Token JWT.
        RealizarTransferenciaCommand command = new RealizarTransferenciaCommand(
                request.billeteraDestinoId(),
                request.monto(),
                request.concepto(),
                idempotencyKey
        );

        // 2. Ejecutar el orquestador transaccional.
        Transaccion transaccion = realizarTransferenciaUseCase.ejecutar(command);

        // 3. Mapear al DTO de salida manteniendo LocalDateTime nativo.
        TransaccionResponse response = new TransaccionResponse(
                transaccion.getId(),
                transaccion.getEstado().name(),
                transaccion.getFecha(), 
                "Transferencia procesada y registrada con éxito."
        );

        // 4. Retornar 201 CREATED según el estándar REST
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}