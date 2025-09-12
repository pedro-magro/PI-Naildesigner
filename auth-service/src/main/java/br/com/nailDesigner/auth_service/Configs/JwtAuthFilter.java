package br.com.nailDesigner.auth_service.Configs;

import br.com.nailDesigner.auth_service.Services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        System.out.println("\n--- [JWT-FILTER-DEBUG] Iniciando filtro para a rota: " + request.getRequestURI() + " ---");

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId; // Alterado de 'username' para 'userId' para maior clareza

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JWT-FILTER-DEBUG] Header 'Authorization' não encontrado ou não começa com 'Bearer'. Continuando como anónimo.");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        System.out.println("[JWT-FILTER-DEBUG] Token extraído: " + jwt.substring(0, 15) + "...");

        try {
            userId = jwtService.extractUsername(jwt); // Lembre-se, este método agora extrai o UUID (subject)
            System.out.println("[JWT-FILTER-DEBUG] ID (Subject) extraído do token: " + userId);

            // Verifica se o ID foi extraído e se não há ninguém já autenticado nesta sessão
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("[JWT-FILTER-DEBUG] Contexto de segurança está vazio. A carregar detalhes do utilizador...");
                
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userId);
                System.out.println("[JWT-FILTER-DEBUG] UserDetails carregado para o utilizador: " + userDetails.getUsername());

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    System.out.println("[JWT-FILTER-DEBUG] SUCESSO: Token é válido. A criar objeto de autenticação.");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Coloca o utilizador autenticado no contexto de segurança
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("[JWT-FILTER-DEBUG] Utilizador autenticado e definido no SecurityContextHolder.");
                } else {
                    System.out.println("[JWT-FILTER-DEBUG] AVISO: A validação do token falhou (isTokenValid retornou false).");
                }
            } else {
                 System.out.println("[JWT-FILTER-DEBUG] ID não extraído do token OU já existe uma autenticação no contexto.");
            }
        } catch (Exception e) {
             System.err.println("[JWT-FILTER-DEBUG] ERRO ao processar o token: " + e.getMessage());
        }
        
        System.out.println("--- [JWT-FILTER-DEBUG] Filtro concluído. A passar para o próximo filtro. ---\n");
        filterChain.doFilter(request, response);
    }
}
