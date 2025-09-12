package br.com.naildesigner.servico_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import br.com.naildesigner.servico_service.config.JwtAuthFilter;

// Importe seu JwtAuthFilter (copiado para este projeto)
// Ex: import br.com.naildesigner.servico_service.filter.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar @PreAuthorize nos controllers se desejar
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter; // O filtro JWT copiado para este projeto

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                // Endpoints Públicos (permitAll)
            	.requestMatchers(new AntPathRequestMatcher("/h2-console-servicos/**")).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/servicos").permitAll()         // Listar todos os serviços
                .requestMatchers(HttpMethod.GET, "/api/servicos/{id}").permitAll()    // Buscar serviço por ID
                .requestMatchers(HttpMethod.GET, "/api/servicos/hello").permitAll()   // Endpoint de teste
                // Endpoints Privados (qualquer usuário autenticado)
                .requestMatchers(HttpMethod.POST, "/api/servicos").authenticated()       // Criar serviço
                .requestMatchers(HttpMethod.PUT, "/api/servicos/{id}").authenticated()   // Atualizar serviço
                .requestMatchers(HttpMethod.DELETE, "/api/servicos/{id}").authenticated()// Excluir serviço

                // Exemplo para console H2 (se habilitado)
                // .requestMatchers("/h2-console-servicos/**").permitAll()

                // Qualquer outra requisição precisa estar autenticada
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        	.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        // Se o console H2 estiver habilitado:
        // http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    // OBS: Este serviço, como Resource Server, normalmente NÃO precisa de:
    // - AuthenticationManager
    // - AuthenticationProvider
    // - PasswordEncoder
    // Pois ele apenas valida tokens JWT e não autentica usuários diretamente.
}
