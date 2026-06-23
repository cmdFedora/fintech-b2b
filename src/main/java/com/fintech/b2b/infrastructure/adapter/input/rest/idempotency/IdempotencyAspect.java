package com.fintech.b2b.infrastructure.adapter.input.rest.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.b2b.domain.model.port.UsuarioAutenticadoPort;
import com.fintech.b2b.infrastructure.adapter.input.rest.dto.TransaccionResponse;
import com.fintech.b2b.infrastructure.adapter.input.rest.dto.TransferenciaRequest;
import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.IdempotencyKeyEntity;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Aspect
@Component
public class IdempotencyAspect {

    private final IdempotencyService idempotencyService;
    private final UsuarioAutenticadoPort usuarioAutenticadoPort;
    private final ObjectMapper objectMapper;

    public IdempotencyAspect(IdempotencyService idempotencyService,
                             UsuarioAutenticadoPort usuarioAutenticadoPort,
                             ObjectMapper objectMapper) {
        this.idempotencyService = idempotencyService;
        this.usuarioAutenticadoPort = usuarioAutenticadoPort;
        this.objectMapper = objectMapper;
    }

    // Intercepta mágicamente el método realizarTransferencia de tu controlador
    @Around("execution(* com.fintech.b2b.infrastructure.adapter.input.rest.TransferenciaController.realizarTransferencia(..)) && args(request, idempotencyKey)")
    public Object interceptar(ProceedingJoinPoint joinPoint, TransferenciaRequest request, UUID idempotencyKey) throws Throwable {

        String clientId = String.valueOf(usuarioAutenticadoPort.getIdUsuarioActual());
        String payloadJson = objectMapper.writeValueAsString(request);
        String key = idempotencyKey.toString();

        // 1. Consultar la Aduana (Base de Datos)
        IdempotencyKeyEntity llave = idempotencyService.procesarLlave(clientId, key, payloadJson);

        // 2. Si la llave ya existía, bloqueamos y respondemos desde el caché
        if (llave != null) {
            if (llave.getEstado() == EstadoIdempotencia.PROCESSING) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Petición en proceso. Por favor espere."));
            }
            if (llave.getRespuestaJson() != null && llave.getEstado() == EstadoIdempotencia.COMPLETED) {
                // Reconstruimos la respuesta original de éxito
                TransaccionResponse cachedResponse = objectMapper.readValue(llave.getRespuestaJson(), TransaccionResponse.class);
                return ResponseEntity.ok(cachedResponse);
            }
        }

        // 3. Si la llave es NUEVA, dejamos pasar la petición al Controlador real
        try {
            Object result = joinPoint.proceed();

            // 4. Si la transferencia fue un éxito, capturamos el JSON generado y lo guardamos
            if (result instanceof ResponseEntity<?> responseEntity) {
                String jsonResponse = objectMapper.writeValueAsString(responseEntity.getBody());
                idempotencyService.actualizarRespuesta(clientId, key, jsonResponse, EstadoIdempotencia.COMPLETED);
            }
            return result;

        } catch (Exception ex) {
            // Si estalla por negocio (ej. Saldo Insuficiente), marcamos la llave como FAILED
            idempotencyService.actualizarRespuesta(clientId, key, ex.getMessage(), EstadoIdempotencia.FAILED);
            throw ex; // Dejamos que el GlobalExceptionHandler atrape el error normalmente
        }
    }
}