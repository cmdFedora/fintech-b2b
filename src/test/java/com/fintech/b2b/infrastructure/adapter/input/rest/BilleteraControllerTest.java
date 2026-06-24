package com.fintech.b2b.infrastructure.adapter.input.rest;

import com.fintech.b2b.application.port.in.ConsultarSaldoUseCase;
import com.fintech.b2b.application.port.in.ObtenerHistorialMovimientosUseCase;
import com.fintech.b2b.domain.model.MovimientoLedger;
import com.fintech.b2b.domain.model.TipoMovimiento;
import com.fintech.b2b.domain.model.UserIdentity;
import com.fintech.b2b.domain.model.port.TokenProviderPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BilleteraController.class)
class BilleteraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarSaldoUseCase consultarSaldoUseCase;

    @MockitoBean
    private ObtenerHistorialMovimientosUseCase historialMovimientosUseCase;
    
    @MockitoBean
    private TokenProviderPort tokenProviderPort;

    private UsernamePasswordAuthenticationToken authMock;
    private UserIdentity identidadMock;

    @BeforeEach
    void setUp() {
        // 1. Replicamos exactamente lo que hace tu JwtAuthenticationFilter
        identidadMock = new UserIdentity(1L, "daniel@b2b.com", "ROLE_EMPLEADO");
        authMock = new UsernamePasswordAuthenticationToken(
                identidadMock,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(identidadMock.rol()))
        );
    }

    @Test
    void debeRetornarSaldoConHttp200() throws Exception {
        // GIVEN
        BigDecimal saldoEsperado = new BigDecimal("1500.50");
        when(consultarSaldoUseCase.ejecutar(identidadMock.id())).thenReturn(saldoEsperado);

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/billeteras/mi-saldo")
                        .with(authentication(authMock))) // Inyectamos la seguridad aquí
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(1500.5));
    }

    @Test
    void debeRetornarHistorialPaginadoConHttp200() throws Exception {
        // GIVEN
        MovimientoLedger movimiento = MovimientoLedger.crear(
                99L, 100L, TipoMovimiento.CREDITO, new BigDecimal("200.00"), new BigDecimal("1700.50")
        );
        Page<MovimientoLedger> paginaMock = new PageImpl<>(List.of(movimiento));

        // Le decimos a Mockito que cuando reciba cualquier objeto Pageable, devuelva nuestra página
        when(historialMovimientosUseCase.ejecutar(eq(identidadMock.id()), any(Pageable.class)))
                .thenReturn(paginaMock);

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/billeteras/historial")
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(authMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tipoMovimiento").value("CREDITO"))
                .andExpect(jsonPath("$.content[0].monto").value(200.0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void debeRechazarPeticionSiNoHayToken() throws Exception {
        // Verificamos que Spring Security bloquee peticiones sin autenticación (HTTP 401)
        mockMvc.perform(get("/api/v1/billeteras/mi-saldo"))
                .andExpect(status().isUnauthorized());
    }
}