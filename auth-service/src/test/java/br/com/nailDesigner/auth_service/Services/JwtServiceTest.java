package br.com.nailDesigner.auth_service.Services;

import br.com.nailDesigner.auth_service.Models.Role;
import io.jsonwebtoken.Claims;
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
    public void deveGerarTokenValido(){

        String token = jwtService.generateToken(new HashMap<>(), user);

        assertNotNull(token);
        assertEquals(user.getId().toString(), jwtService.extractUserId(token));
        assertTrue(jwtService.isTokenValid(token, user));

    }
}
