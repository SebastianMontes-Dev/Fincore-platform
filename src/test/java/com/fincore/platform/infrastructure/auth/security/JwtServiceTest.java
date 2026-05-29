package com.fincore.platform.infrastructure.auth.security;

import org.junit.jupiter.api.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString(
                "clave-secreta-para-pruebas-unitarias-jwt".getBytes());
        jwtService = new JwtService(secret, 15L, 7L);
    }

    @Test
    void generarAccessTokenConDatos() {
        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        String token = jwtService.generarAccessToken(userId, "test@test.com", empresaId, "OWNER");

        assertNotNull(token);
        assertTrue(jwtService.esTokenValido(token));
        assertEquals(userId, jwtService.obtenerUsuarioId(token));
        assertEquals(empresaId, jwtService.obtenerTenantId(token));
        assertEquals("OWNER", jwtService.obtenerRol(token));
    }

    @Test
    void generarRefreshToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generarRefreshToken(userId);
        assertTrue(jwtService.esTokenValido(token));
        assertEquals(userId, jwtService.obtenerUsuarioId(token));
    }

    @Test
    void tokenFalsoEsInvalido() {
        assertFalse(jwtService.esTokenValido("inventado"));
        assertFalse(jwtService.esTokenValido(null));
    }

    @Test
    void accessTokenMsEsPositivo() {
        assertTrue(jwtService.getAccessTokenMs() > 0);
    }
}
