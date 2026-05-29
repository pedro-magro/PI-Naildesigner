package br.com.naildesigner.servico_service.config;

import br.com.naildesigner.servico_service.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    private final TestableJwtAuthFilter jwtAuthFilter = new TestableJwtAuthFilter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ct218_deveAutenticarUsuarioQuandoTokenForValido() throws ServletException, IOException {
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtService", jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        request.setRequestURI("/api/servicos");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("token-valido"))
                .thenReturn("usuario-teste");

        when(jwtService.isTokenSignatureAndExpirationValid("token-valido"))
                .thenReturn(true);

        when(jwtService.extractRoles("token-valido"))
                .thenReturn(List.of("ROLE_ADMIN"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("usuario-teste", authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void ct219_deveContinuarSemAutenticarQuandoNaoHouverBearerToken() throws ServletException, IOException {
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtService", jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/servicos");

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