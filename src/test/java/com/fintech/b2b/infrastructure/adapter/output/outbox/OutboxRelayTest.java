package com.fintech.b2b.infrastructure.adapter.output.outbox;

import com.fintech.b2b.domain.model.OutboxEvent;
import com.fintech.b2b.domain.model.port.OutboxEventRepositoryPort;
import com.fintech.b2b.infrastructure.adapter.output.persistence.entity.OutboxEventEntity;
import com.fintech.b2b.infrastructure.adapter.output.persistence.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev") // Usamos tu PostgreSQL real configurado en dev
class OutboxRelayTest {

    @Autowired
    private OutboxEventRepositoryPort repositoryPort;

    @Autowired
    private OutboxEventJpaRepository jpaRepository; // Lo inyectamos directo para poder leer por ID al final

    @Autowired
    private OutboxRelayTask relayTask;

    @Test
    @DisplayName("El Cartero debe encontrar eventos PENDING, enviarlos y sellarlos como PROCESSED")
    void pruebaCarteroOutbox() {
        
        // 1. PREPARACIÓN: Creamos un evento directamente en el Dominio y lo guardamos
        String payloadDePrueba = "{\"mensaje\": \"Este es un paquete de prueba para el Cartero\"}";
        OutboxEvent evento = OutboxEvent.crear("TestAggregate", "999", "PruebaCompletada", payloadDePrueba);
        
        OutboxEvent eventoGuardado = repositoryPort.guardar(evento);

        assertNotNull(eventoGuardado.getId(), "El evento debió generarse con un ID en la BD");
        assertEquals(OutboxEvent.EstadoOutbox.PENDING, eventoGuardado.getEstado(), "El evento debe nacer como PENDING");

        // 2. ACCIÓN: En lugar de esperar 5 segundos, llamamos al método del Cartero manualmente
        relayTask.despacharEventosPendientes();

        // 3. VERIFICACIÓN: Vamos a la base de datos a ver qué le pasó a nuestro evento
        OutboxEventEntity eventoSellado = jpaRepository.findById(eventoGuardado.getId())
                .orElseThrow(() -> new RuntimeException("El evento desapareció de la BD"));

        // Comprobamos que el Cartero hizo su trabajo
        assertEquals(OutboxEvent.EstadoOutbox.PROCESSED, eventoSellado.getEstado(), 
                "El Cartero no cambió el estado a PROCESSED");
        assertNotNull(eventoSellado.getFechaProcesamiento(), 
                "El Cartero no le puso el sello de tiempo de procesamiento");
    }
}