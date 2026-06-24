package com.fintech.b2b.application.service;

import com.fintech.b2b.application.port.in.RealizarTransferenciaCommand;
import com.fintech.b2b.domain.model.Billetera;
import com.fintech.b2b.domain.model.port.BilleteraRepositoryPort;
import com.fintech.b2b.domain.model.port.UsuarioAutenticadoPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("dev") // Impactamos la DB real de PostgreSQL
class TransferenciaConcurrenciaTest {

    @Autowired
    private RealizarTransferenciaUseCaseImpl transferenciaUseCase;

    @Autowired
    private BilleteraRepositoryPort billeteraRepositoryPort;

    @Autowired
    private JdbcTemplate jdbcTemplate; // Para inyectar usuarios rápido sin violar Foreign Keys

    @MockitoBean
    private UsuarioAutenticadoPort usuarioAutenticadoPort; // Burlamos la seguridad JWT para el test

    @Test
    @DisplayName("Concurrencia Extrema: 50 hilos intentan gastar el mismo saldo al mismo tiempo")
    void pruebaConcurrenciaDoubleSpending() throws InterruptedException {
    	
    	jdbcTemplate.execute("DELETE FROM movimientos_ledger");
        jdbcTemplate.execute("DELETE FROM transacciones");
        jdbcTemplate.execute("DELETE FROM billeteras WHERE usuario_id IN (998, 999)");
        
        // 1. PREPARACIÓN (Data Setup)
        // Inyectamos usuarios "dummy" directamente a la DB para satisfacer la llave foránea
        jdbcTemplate.execute("INSERT INTO usuarios (id, email, password_hash, rol) VALUES (998, 'hacker@test.com', 'x', 'ROLE_EMPLEADO') ON CONFLICT (email) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO usuarios (id, email, password_hash, rol) VALUES (999, 'complice@test.com', 'x', 'ROLE_EMPLEADO') ON CONFLICT (email) DO NOTHING");

        // Creamos billeteras frescas. Origen tiene $100, Destino tiene $0.
        Billetera billeteraOrigen = billeteraRepositoryPort.guardar(
                new Billetera(null, 998L, new BigDecimal("100.00"), 0L)
        );
        Billetera billeteraDestino = billeteraRepositoryPort.guardar(
                new Billetera(null, 999L, new BigDecimal("0.00"), 0L)
        );

        Long idOrigen = billeteraOrigen.getId();
        Long idDestino = billeteraDestino.getId();

        // Simulamos que quien hace la petición en el controlador es el "hacker" (998)
        when(usuarioAutenticadoPort.getIdUsuarioActual()).thenReturn(998L);

        // 2. CONFIGURACIÓN DEL ESTRÉS (Hilos y Compuertas)
        int numeroDeHilos = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numeroDeHilos);
        
        // CountDownLatch funciona como la barrera de una carrera de caballos
        CountDownLatch compuertaDeSalida = new CountDownLatch(1);
        CountDownLatch compuertaDeLlegada = new CountDownLatch(numeroDeHilos);

        AtomicInteger exitosas = new AtomicInteger(0);
        AtomicInteger fallidas = new AtomicInteger(0);

        // 3. EJECUCIÓN: Preparamos a los 50 "caballos" en sus posiciones
        for (int i = 0; i < numeroDeHilos; i++) {
            executor.submit(() -> {
                try {
                    // El hilo se queda congelado aquí, esperando el disparo
                    compuertaDeSalida.await(); 
                    
                    // IMPORTANTE: Generamos UUIDs distintos para evadir tu escudo de Idempotencia.
                    // Queremos forzar el ataque directo a la Base de Datos para probar el Bloqueo Optimista.
                    RealizarTransferenciaCommand command = new RealizarTransferenciaCommand(
                            idDestino,
                            new BigDecimal("100.00"),
                            "Ataque concurrente",
                            UUID.randomUUID()
                    );
                    
                    transferenciaUseCase.ejecutar(command);
                    exitosas.incrementAndGet(); // Si llega aquí, la base de datos se dejó engañar
                    
                } catch (Exception e) {
                 // Si llega aquí, el Bloqueo Optimista (o tu excepción de saldo) hizo su trabajo
                    fallidas.incrementAndGet();
                } finally {
                    compuertaDeLlegada.countDown(); // El hilo avisa que terminó su intento
                }
            });
        }

        // Abrimos la compuerta. Los 50 hilos atacan tu base de datos al mismo nanosegundo.
        compuertaDeSalida.countDown(); 
        
        // El hilo principal se sienta a esperar que termine la masacre
        compuertaDeLlegada.await(); 
        executor.shutdown();

        // 4. VERIFICACIÓN (Auditoría Forense)
        Billetera origenPostAtaque = billeteraRepositoryPort.buscarPorId(idOrigen).orElseThrow();
        Billetera destinoPostAtaque = billeteraRepositoryPort.buscarPorId(idDestino).orElseThrow();

        // Comprobamos la integridad absoluta del sistema:
        assertEquals(1, exitosas.get(), "¡Peligro! Solo 1 de las 50 transferencias debió procesarse");
        assertEquals(49, fallidas.get(), "49 intentos debieron ser bloqueados por concurrencia");

        // El dinero final es el que dicta la verdad:
        assertEquals(0, new BigDecimal("0.00").compareTo(origenPostAtaque.getSaldo().monto()), "El hacker no debe quedar con saldo negativo");
        assertEquals(0, new BigDecimal("100.00").compareTo(destinoPostAtaque.getSaldo().monto()), "El cómplice solo debió recibir $100");
    }
}