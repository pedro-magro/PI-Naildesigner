package br.com.messaging.messaging.Config;

import br.com.messaging.messaging.Services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private TestableJwtAuthFilter jwtAuthFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        filterChain = mock(FilterChain.class);

        jwtAuthFilter = new TestableJwtAuthFilter();
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtService", jwtService);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ct220_deveAutenticarQuandoTokenForValido() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("Authorization", "Bearer token-valido");

        when(jwtService.extractUsername("token-valido"))
                .thenReturn("usuario-teste");
        when(jwtService.isTokenSignatureAndExpirationValid("token-valido"))
                .thenReturn(true);
        when(jwtService.extractRoles("token-valido"))
                .thenReturn(List.of("ROLE_ADMIN"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("usuario-teste", authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void ct221_deveSeguirSemAutenticarQuandoNaoHouverBearerToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService, never()).extractUsername(anyString());
        verify(jwtService, never()).isTokenSignatureAndExpirationValid(anyString());
        verify(jwtService, never()).extractRoles(anyString());
        verify(filterChain).doFilter(request, response);
    }

    private static class TestableJwtAuthFilter extends JwtAuthFilter {

        @Override
        public void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            super.doFilterInternal(request, response, filterChain);
        }
    }
}