package com.fintech.b2b.infrastructure.adapter.output.outbox;

import com.fintech.b2b.domain.model.OutboxEvent;
import com.fintech.b2b.domain.model.port.OutboxEventRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxRelayTask {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayTask.class);
    private final OutboxEventRepositoryPort repositoryPort;

    public OutboxRelayTask(OutboxEventRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    // Se ejecuta cada 5000 milisegundos (5 segundos) automáticamente
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void despacharEventosPendientes() {
        
        // 1. Buscamos un lote manejable de eventos (50) para no colapsar la memoria del servidor
        List<OutboxEvent> pendientes = repositoryPort.buscarPendientes(50);

        if (pendientes.isEmpty()) {
            return; // No hay paquetes, seguimos durmiendo
        }

        log.info("📬 Iniciando despacho de {} eventos Outbox...", pendientes.size());

        for (OutboxEvent evento : pendientes) {
            try {
                // 2. SIMULAR EL ENVÍO AL EXTERIOR
                // Aquí iría el código real de rabbitTemplate.convertAndSend(...) o kafkaTemplate.send(...)
                log.info(">> [BROKER EXTERNO] Recibiendo Evento: {}", evento.getEventType());
                log.info(">> [BROKER EXTERNO] Payload: {}", evento.getPayload());
                
                // Simulamos un micro-retardo de red para la prueba
                Thread.sleep(100);

                // 3. Éxito: Sellamos el paquete
                evento.marcarComoProcesado();
                repositoryPort.guardar(evento);
                log.info("<< Evento Outbox [{}] sellado como PROCESSED.", evento.getId());

            } catch (Exception e) {
                // 4. Falla: El sistema exterior se cayó. 
                // Lo marcamos como FAILED para que soporte técnico (o un proceso de reintento) lo revise luego.
                log.error("!! Error enviando Evento Outbox [{}]: {}", evento.getId(), e.getMessage());
                evento.marcarComoFallido();
                repositoryPort.guardar(evento);
            }
        }
    }
}