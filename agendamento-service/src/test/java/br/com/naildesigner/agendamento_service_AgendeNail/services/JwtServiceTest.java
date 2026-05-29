package br.com.naildesigner.agendamento_service_AgendeNail.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET_KEY =
            "dGVzdGVzZWNyZXR0ZXN0ZXNlY3JldHRlc3Rlc2VjcmV0dGVzdA==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET_KEY);
    }

    @Test
    void ct173_deveExtrairUserIdValidoDoToken() {
        UUID userId = UUID.randomUUID();

        String token = gerarTokenValido(userId);

        UUID resultado = jwtService.extractUserId(token);

        assertEquals(userId, resultado);
    }

    @Test
    void ct174_deveRetornarFalseQuandoTokenForInvalido() {
        boolean resultado =
                jwtService.isTokenSignatureAndExpirationValid("token-invalido");

        assertFalse(resultado);
    }

    private String gerarTokenValido(UUID userId) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + 1000 * 60 * 60);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("userId", userId.toString())
                .claim("roles", List.of("ROLE_USER"))
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
