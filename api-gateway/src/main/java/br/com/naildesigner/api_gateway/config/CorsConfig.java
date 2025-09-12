package br.com.naildesigner.api_gateway.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource; // Importar java.util.Arrays

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // 1. Origens Permitidas:
        // ESSENCIAL: Substitua "http://localhost:8082" pela URL real do seu frontend.
        // Se você não sabe a porta exata ainda ou está em desenvolvimento,
        // pode usar "http://localhost:*" para permitir qualquer porta no localhost.
        // PARA PRODUÇÃO: SEMPRE USE A URL EXATA DO SEU FRONTEND (ex: "https://seufrotend.com.br")
        corsConfig.setAllowedOriginPatterns(Arrays.asList("http://localhost:8085", "http://localhost:*")); // Permite seu frontend e qualquer porta no localhost

        // Ou, se realmente precisar permitir *qualquer* origem (NÃO RECOMENDADO PARA PRODUÇÃO!):
        // corsConfig.addAllowedOriginPattern("*"); 

        // 2. Métodos HTTP Permitidos:
        // Permite GET, POST, PUT, DELETE, OPTIONS (OPTIONS é usado para preflight requests CORS)
        corsConfig.addAllowedMethod("*"); 

        // 3. Cabeçalhos Permitidos:
        // Permite todos os cabeçalhos nas requisições
        corsConfig.addAllowedHeader("*");

        // 4. Credenciais (Cookies, Autenticação):
        // Permite o envio de credenciais (como cookies ou cabeçalhos de autorização)
        corsConfig.setAllowCredentials(true); 

        // 5. Exposição de Cabeçalhos (Opcional):
        // Se sua API retornar cabeçalhos personalizados que o frontend precisa ler, liste-os aqui.
        // corsConfig.addExposedHeader("X-My-Custom-Header");

        // 6. Tempo de vida do preflight request (em segundos):
        // O navegador "cacheia" a permissão CORS por esse tempo, reduzindo preflight requests.
        corsConfig.setMaxAge(3600L); // 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuração CORS a TODOS os caminhos (/**) que passam pelo Gateway
        source.registerCorsConfiguration("/**", corsConfig); 

        return new CorsWebFilter(source);
    }
}