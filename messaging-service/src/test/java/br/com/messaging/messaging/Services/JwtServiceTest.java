package br.com.messaging.messaging.Services;

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
    void ct216_deveExtrairClaimsDeTokenValido() {
        String token = gerarTokenValido();

        String username = jwtService.extractUsername(token);
        List<String> roles = jwtService.extractRoles(token);

        assertEquals("usuario-teste", username);
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void ct217_deveRetornarFalseQuandoTokenForInvalido() {
        boolean resultado =
                jwtService.isTokenSignatureAndExpirationValid("token-invalido");

        assertFalse(resultado);
    }

    private String gerarTokenValido() {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + 1000 * 60 * 60);

        return Jwts.builder()
                .setSubject("usuario-teste")
                .claim("roles", List.of("ROLE_ADMIN"))
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