package com.fintech.b2b.infrastructure.adapter.input.rest.idempotency;

import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.IdempotencyKeyEntity;
import com.fintech.b2b.infrastructure.adapter.output.persistence.repository.IdempotencyKeyJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
public class IdempotencyService {

    private final IdempotencyKeyJpaRepository repository;

    public IdempotencyService(IdempotencyKeyJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Evalúa la llave. Retorna la entidad si ya existía (para devolver la respuesta cacheada),
     * o retorna null si es una petición nueva (y la deja registrada como PROCESSING).
     */
    public IdempotencyKeyEntity procesarLlave(String clientId, String idempotencyKey, String payload) {
        
        String payloadHash = generarHash(payload);
        Optional<IdempotencyKeyEntity> llaveExistente = repository.findByClientIdAndIdempotencyKey(clientId, idempotencyKey);

        if (llaveExistente.isPresent()) {
            IdempotencyKeyEntity entity = llaveExistente.get();
            
            // Regla de Rúbrica: Rechazar reutilización de llave con payload diferente
            if (!entity.getPayloadHash().equals(payloadHash)) {
                throw new IllegalStateException("CONFLICTO: La llave de idempotencia ya fue utilizada con un payload diferente.");
            }
            return entity; // Retornamos la llave existente para que el Interceptor decida qué hacer
        }

        // Si no existe, creamos una nueva en estado PROCESSING
        IdempotencyKeyEntity nuevaLlave = new IdempotencyKeyEntity();
        nuevaLlave.setClientId(clientId);
        nuevaLlave.setIdempotencyKey(idempotencyKey);
        nuevaLlave.setPayloadHash(payloadHash);
        nuevaLlave.setEstado(EstadoIdempotencia.PROCESSING);

        try {
            repository.saveAndFlush(nuevaLlave);
            return null; // Null indica que es una petición 100% nueva y puede continuar al Controlador
            
        } catch (DataIntegrityViolationException e) {
            // Regla de Rúbrica: Evitar dobles cobros por reintentos simultáneos
            // Si dos hilos llegan aquí al mismo tiempo, PostgreSQL lanzará esta excepción en el segundo hilo 
            // gracias al índice UNIQUE(client_id, idempotency_key) de tu V1.
            throw new IllegalStateException("CONCURRENCIA: La transacción ya está siendo procesada por otra petición.");
        }
    }

    public void actualizarRespuesta(String clientId, String idempotencyKey, String jsonResponse, EstadoIdempotencia estado) {
        repository.findByClientIdAndIdempotencyKey(clientId, idempotencyKey).ifPresent(entity -> {
            entity.setEstado(estado);
            entity.setRespuestaJson(jsonResponse);
            repository.save(entity);
        });
    }

    private String generarHash(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error fatal: Algoritmo SHA-256 no disponible en el sistema.", e);
        }
    }
}