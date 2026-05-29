package br.com.naildesigner.agendamento_service_AgendeNail.config;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FeignClientInterceptorTest {

    private final FeignClientInterceptor interceptor = new FeignClientInterceptor();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ct175_deveAdicionarBearerTokenQuandoAuthenticationPossuirJwt() {
        Jwt jwt = Jwt.withTokenValue("token-valido")
                .header("alg", "none")
                .subject("user-id")
                .build();

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(jwt, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        RequestTemplate requestTemplate = new RequestTemplate();

        interceptor.apply(requestTemplate);

        Collection<String> authorizationHeaders =
                requestTemplate.headers().get("Authorization");

        assertNotNull(authorizationHeaders);
        assertTrue(authorizationHeaders.contains("Bearer token-valido"));
    }

    @Test
    void ct176_naoDeveAdicionarAuthorizationQuandoNaoHouverAuthentication() {
        SecurityContextHolder.clearContext();

        RequestTemplate requestTemplate = new RequestTemplate();

        interceptor.apply(requestTemplate);

        assertFalse(requestTemplate.headers().containsKey("Authorization"));
    }
}
