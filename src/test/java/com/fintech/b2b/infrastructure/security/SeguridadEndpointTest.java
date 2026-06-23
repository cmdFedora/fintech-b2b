package com.fintech.b2b.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev") // Usamos el perfil dev para que se conecte a la BD de pruebas
class SeguridadEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Debe retornar 401 Unauthorized al intentar transferir sin token JWT")
    void cuandoPeticionNoTieneToken_entoncesRetorna401() throws Exception {
        
        // Similamos el JSON (DTO) de la transferencia
        String bodyRequest = """
                {
                  "billeteraDestinoId": 2,
                  "monto": 500.00,
                  "concepto": "Pago de prueba sin seguridad"
                }
                """;

        // Atacamos el endpoint sin incluir el Header de Authorization
        mockMvc.perform(post("/api/v1/transferencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyRequest)
                .header("Idempotency-Key", "123e4567-e89b-12d3-a456-426614174000"))
        		.andExpect(status().isForbidden());
    }
}