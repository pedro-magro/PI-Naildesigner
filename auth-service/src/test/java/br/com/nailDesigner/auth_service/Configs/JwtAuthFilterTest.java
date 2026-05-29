package br.com.nailDesigner.auth_service.Configs;

import br.com.nailDesigner.auth_service.Services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private final TestableJwtAuthFilter jwtAuthFilter = new TestableJwtAuthFilter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ct151_deveAutenticarUsuarioQuandoTokenForValido() throws ServletException, IOException {
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtService", jwtService);
        ReflectionTestUtils.setField(jwtAuthFilter, "userDetailsService", userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        request.setRequestURI("/auth/me");

        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("user-id-123")
                .password("senha")
                .authorities("ROLE_USER")
                .build();

        when(jwtService.extractUserId("token-valido"))
                .thenReturn("user-id-123");

        when(userDetailsService.loadUserByUsername("user-id-123"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("token-valido", userDetails))
                .thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void ct152_deveContinuarSemAutenticarQuandoNaoHouverBearerToken() throws ServletException, IOException {
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtService", jwtService);
        ReflectionTestUtils.setField(jwtAuthFilter, "userDetailsService", userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/me");

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService, never()).extractUserId(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void ct153_deveContinuarSemAutenticarQuandoTokenForInvalido() throws ServletException, IOException {
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtService", jwtService);
        ReflectionTestUtils.setField(jwtAuthFilter, "userDetailsService", userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");
        request.setRequestURI("/auth/me");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUserId("token-invalido"))
                .thenThrow(new RuntimeException("token inválido"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService).extractUserId("token-invalido");
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    private static class TestableJwtAuthFilter extends JwtAuthFilter {
        @Override
        public void doFilterInternal(
                jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            super.doFilterInternal(request, response, filterChain);
        }
    }
}
