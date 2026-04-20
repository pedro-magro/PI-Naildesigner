package br.com.naildesigner.api_gateway.config;

import io.jsonwebtoken.io.Decoders; // Import para JJWT
import static org.springframework.security.config.Customizer.withDefaults;
import javax.crypto.SecretKey; // Import para SecretKey
import javax.crypto.spec.SecretKeySpec; // Import para SecretKeySpec
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // Import para @Value
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder; // Import para NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder; // Import para ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(GatewaySecurityConfig.class);

    @Value("${jwt.secret.key}") // Pega a chave do application.yml
    private String jwtSecretKeyString;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        logger.info("Carregando segurança do API Gateway com validação JWT.");

        http
        	.cors(withDefaults()) // Diz ao Spring Security para usar a configuração CORS definida em outro lugar (seu bean)
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchange -> exchange
            	.pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/api/auth/register").permitAll()
                .pathMatchers("/api/auth/login").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/servicos").permitAll() // Listar serviços
                .pathMatchers(HttpMethod.GET, "/api/servicos/{id}").permitAll() // Detalhe do serviç
                // Adicione aqui qualquer outra rota do Gateway que seja pública
                .anyExchange().authenticated()
            )
            // Configura o Gateway como um Resource Server que valida JWTs
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
            

        return http.build();
    }

    // Bean para criar o decodificador de JWT usando a chave secreta
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        SecretKey secretKey = getSignInKey();
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

    // Método auxiliar para converter a string da chave secreta (Base64) em um objeto SecretKey
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKeyString);
        // O algoritmo DEVE ser compatível com o usado para assinar o token (HS256)
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");
    }
}
