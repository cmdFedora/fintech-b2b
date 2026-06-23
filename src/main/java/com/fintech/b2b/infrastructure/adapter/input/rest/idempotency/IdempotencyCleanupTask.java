package com.fintech.b2b.infrastructure.adapter.input.rest.idempotency;

import com.fintech.b2b.infrastructure.adapter.output.persistence.repository.IdempotencyKeyJpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class IdempotencyCleanupTask {

    private final IdempotencyKeyJpaRepository repository;

    public IdempotencyCleanupTask(IdempotencyKeyJpaRepository repository) {
        this.repository = repository;
    }

    // Se ejecuta automáticamente cada hora (cron: segundo 0, minuto 0, cada hora)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void limpiarLlavesExpiradas() {
        // Spring Data JPA permite borrar usando métodos derivados si los declaramos en el repositorio,
        // o podemos iterar. Para no tocar el repositorio, usemos una lógica funcional:
        repository.findAll().stream()
                .filter(key -> key.getFechaExpiracion().isBefore(LocalDateTime.now()))
                .forEach(repository::delete);
                
        // Nota: En un entorno de altísimo volumen, declararíamos un @Modifying @Query 
        // en el repositorio para borrar todo con una sola sentencia SQL.
    }
}