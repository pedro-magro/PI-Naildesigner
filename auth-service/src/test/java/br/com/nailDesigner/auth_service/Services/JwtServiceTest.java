package br.com.nailDesigner.auth_service.Services;

import br.com.nailDesigner.auth_service.Models.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import br.com.nailDesigner.auth_service.Models.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class JwtServiceTest {

    private final JwtService jwtService = new JwtService();
    private User user;

    @BeforeEach
    void setUp() throws IOException {

        ReflectionTestUtils.setField(jwtService,"SECRET_KEY" ,"dGVzdGVzZWNyZXR0ZXN0ZXNlY3JldHRlc3Rlc2VjcmV0dGVzdA==");
        user = new User("pedro", "123", "pedro@gmail.com", "123", Role.USER);
    }


    @Test
    public void ct99_deveGerarTokenValido(){

        String token = jwtService.generateToken(new HashMap<>(), user);

        assertNotNull(token);
        assertEquals(user.getId().toString(), jwtService.extractUserId(token));
        assertTrue(jwtService.isTokenValid(token, user));

    }

    @Test
    public void ct99A_deveGerarTokenComRolesValidas(){
        String token = jwtService.generateToken(new HashMap<>(), user);

        Claims claims = jwtService.extractClaim(token, c -> c);

        List<String> roles = claims.get("roles", List.class);
        assertNotNull(claims);
        assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    public void ct99B_deveGerarTokenComUserIdValido(){
        String token = jwtService.generateToken(new HashMap<>(), user);

        Claims claims = jwtService.extractClaim(token, c -> c);
        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals(user.getId().toString(), String.valueOf(claims.get("userId")));
    }

    @Test
    public void ct99C_devePreservarClaimsExtrasAoGerarToken(){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("origem", "forntend");
        extraClaims.put("destino", "pedro");

        String token = jwtService.generateToken(extraClaims, user);

        Claims claims = jwtService.extractClaim(token, c -> c);

        assertEquals("forntend", claims.get("origem"));
        assertEquals("pedro", claims.get("destino"));
    }

    @Test
    public void ct99D_deveRetornarFalseQuandoTokenForDeOutroUsuario(){
        String token = jwtService.generateToken(new HashMap<>(), user);

        User otherUser = new User("Paulo", "12345678", "paulo@email.com", "1140028922", Role.USER);

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    public void ct99E_deveExtrairIdDoToken(){
        String token = jwtService.generateToken(new HashMap<>(), user);
        assertEquals(user.getId().toString(), jwtService.extractUserId(token));
    }

    @Test
    public void ct99F_deveLancarExecaoQuandoTentarExtrairDeTokenInvalido(){
        String token = "token-invalido";

        assertThrows(Exception.class, () -> jwtService.extractUserId(token));
    }

    @Test
    public void ct99G_deveGerarTokenComSucessoSemClaimsExtra(){
        String token = jwtService.generateToken(new HashMap<>(), user);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, user));
        assertEquals(user.getId().toString(), jwtService.extractUserId(token));
    }






}
