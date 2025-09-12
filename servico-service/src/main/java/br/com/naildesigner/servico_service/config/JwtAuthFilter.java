package br.com.naildesigner.servico_service.config;

import br.com.naildesigner.servico_service.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    // NÃO PRECISAMOS MAIS DO UserDetailsService AQUI

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Se não há token, ou não é Bearer, passa para o próximo filtro.
        // Endpoints públicos (permitAll) serão liberados pelo Spring Security mais adiante.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Valida a assinatura e expiração do token
                if (jwtService.isTokenSignatureAndExpirationValid(jwt)) {
                    // Extrai as roles diretamente do token
                    List<String> roles = jwtService.extractRoles(jwt);
                    List<SimpleGrantedAuthority> authorities = roles != null
                            ? roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                            : java.util.Collections.emptyList();

                    // O 'principal' agora é o username (String), ou um objeto UserDetails customizado
                    // que você pode criar aqui apenas com username e authorities se precisar de um objeto UserDetails.
                    // Para a maioria dos casos, o username é suficiente como principal.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, // Principal é o username
                            null,     // Credenciais são nulas para autenticação baseada em token
                            authorities // Autoridades extraídas do token
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // logger.warn("Erro ao processar token JWT no messaging-service: {}", e.getMessage());
            SecurityContextHolder.clearContext(); // Limpa o contexto em caso de erro no token
        }

        filterChain.doFilter(request, response);
    }
}
