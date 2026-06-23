package com.fintech.b2b.infrastructure.adapter.input.rest.idempotency;

import com.fintech.b2b.application.port.in.RealizarTransferenciaUseCase;
import com.fintech.b2b.domain.model.EstadoTransaccion;
import com.fintech.b2b.domain.model.Transaccion;
import com.fintech.b2b.domain.model.port.UsuarioAutenticadoPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) 
@ActiveProfiles("dev")
class IdempotenciaFlujoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioAutenticadoPort usuarioAutenticadoPort;

    // Agregamos un Mock del Caso de Uso para aislar la base de datos financiera
    @MockitoBean
    private RealizarTransferenciaUseCase realizarTransferenciaUseCase;

    @Test
    @DisplayName("Debe procesar la primera vez, devolver caché en la segunda, y fallar si cambian el payload")
    void pruebaIntegralIdempotencia() throws Exception {
        
        Mockito.when(usuarioAutenticadoPort.getIdUsuarioActual()).thenReturn(1L);

        // Simulamos que el Caso de Uso hace su trabajo financiero perfectamente
        Transaccion transaccionExitosa = Mockito.mock(Transaccion.class);
        Mockito.when(transaccionExitosa.getId()).thenReturn(999L);
        Mockito.when(transaccionExitosa.getEstado()).thenReturn(EstadoTransaccion.COMPLETED);
        Mockito.when(transaccionExitosa.getFecha()).thenReturn(LocalDateTime.now());
        Mockito.when(realizarTransferenciaUseCase.ejecutar(Mockito.any())).thenReturn(transaccionExitosa);

        String idempotencyKey = UUID.randomUUID().toString();
        
        String payloadOriginal = """
                {
                  "billeteraDestinoId": 2,
                  "monto": 500.00,
                  "concepto": "Pago de servicios"
                }
                """;

        String payloadHackeado = """
                {
                  "billeteraDestinoId": 2,
                  "monto": 9000.00,
                  "concepto": "Pago de servicios"
                }
                """;

        // --- INTENTO 1: Petición Original (Debe devolver 201 CREATED dictado por el Controller)
        mockMvc.perform(post("/api/v1/transferencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadOriginal)
                .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isCreated());

        // --- INTENTO 2: Doble Clic (Debe ser interceptado por AOP y devolver 200 OK desde el caché)
        mockMvc.perform(post("/api/v1/transferencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadOriginal)
                .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk()); 

        // --- INTENTO 3: Intento de Hackeo de Payload (AOP detecta Hash diferente y lanza 500)
        mockMvc.perform(post("/api/v1/transferencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadHackeado)
                .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isInternalServerError());
    }
}