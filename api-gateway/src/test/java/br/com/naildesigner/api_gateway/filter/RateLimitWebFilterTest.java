package br.com.naildesigner.api_gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import br.com.naildesigner.api_gateway.config.RateLimitProperties;
import br.com.naildesigner.api_gateway.service.RateLimitBucketService;
import reactor.core.publisher.Mono;

class RateLimitWebFilterTest {

    @Test
    void devePermitirRequisicaoQuandoBucketTemTokens() {
        RateLimitWebFilter filter = new RateLimitWebFilter(
            properties(true, 2, 2, 60, true),
            new RateLimitBucketService(properties(true, 2, 2, 60, true))
        );
        TrackingWebFilterChain chain = new TrackingWebFilterChain();
        MockServerWebExchange exchange = exchange(HttpMethod.GET, "/api/agendamentos/1", "10.0.0.1");

        filter.filter(exchange, chain).block();

        assertTrue(chain.wasCalled());
        assertEquals("1", exchange.getResponse().getHeaders().getFirst("X-Rate-Limit-Remaining"));
        assertEquals(null, exchange.getResponse().getStatusCode());
    }

    @Test
    void deveBloquearRequisicaoQuandoBucketEstiverExausto() {
        RateLimitProperties properties = properties(true, 1, 1, 60, true);
        RateLimitWebFilter filter = new RateLimitWebFilter(properties, new RateLimitBucketService(properties));

        MockServerWebExchange firstExchange = exchange(HttpMethod.GET, "/api/agendamentos/1", "10.0.0.1");
        MockServerWebExchange secondExchange = exchange(HttpMethod.GET, "/api/agendamentos/1", "10.0.0.1");
        TrackingWebFilterChain firstChain = new TrackingWebFilterChain();
        TrackingWebFilterChain secondChain = new TrackingWebFilterChain();

        filter.filter(firstExchange, firstChain).block();
        filter.filter(secondExchange, secondChain).block();

        assertTrue(firstChain.wasCalled());
        assertFalse(secondChain.wasCalled());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, secondExchange.getResponse().getStatusCode());
        assertEquals(
            "{\"message\":\"Limite de requisições excedido. Tente novamente em instantes.\"}",
            secondExchange.getResponse().getBodyAsString().block()
        );
        assertNotNull(secondExchange.getResponse().getHeaders().getFirst("Retry-After"));
    }

    @Test
    void deveIgnorarCaminhosPublicosConfigurados() {
        RateLimitProperties properties = properties(true, 1, 1, 60, true);
        properties.getPublicPaths().add("/api/auth/login");

        RateLimitWebFilter filter = new RateLimitWebFilter(properties, new RateLimitBucketService(properties));
        TrackingWebFilterChain chain = new TrackingWebFilterChain();
        MockServerWebExchange exchange = exchange(HttpMethod.POST, "/api/auth/login", "10.0.0.1");

        filter.filter(exchange, chain).block();

        assertTrue(chain.wasCalled());
        assertEquals(null, exchange.getResponse().getStatusCode());
        assertEquals(null, exchange.getResponse().getHeaders().getFirst("X-Rate-Limit-Remaining"));
    }

    @Test
    void deveIgnorarRequisicoesOptions() {
        RateLimitProperties properties = properties(true, 1, 1, 60, true);
        RateLimitWebFilter filter = new RateLimitWebFilter(properties, new RateLimitBucketService(properties));
        TrackingWebFilterChain chain = new TrackingWebFilterChain();
        MockServerWebExchange exchange = exchange(HttpMethod.OPTIONS, "/api/agendamentos/1", "10.0.0.1");

        filter.filter(exchange, chain).block();

        assertTrue(chain.wasCalled());
        assertEquals(null, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldUseDifferentBucketsPerPathWhenEnabled() {
        RateLimitProperties properties = properties(true, 1, 1, 60, true);
        RateLimitWebFilter filter = new RateLimitWebFilter(properties, new RateLimitBucketService(properties));

        MockServerWebExchange firstPath = exchange(HttpMethod.GET, "/api/agendamentos/1", "10.0.0.1");
        MockServerWebExchange secondPath = exchange(HttpMethod.GET, "/api/bloqueios/1", "10.0.0.1");
        TrackingWebFilterChain firstChain = new TrackingWebFilterChain();
        TrackingWebFilterChain secondChain = new TrackingWebFilterChain();

        filter.filter(firstPath, firstChain).block();
        filter.filter(secondPath, secondChain).block();

        assertTrue(firstChain.wasCalled());
        assertTrue(secondChain.wasCalled());
    }

    @Test
    void shouldShareBucketAcrossPathsWhenPathIsNotPartOfKey() {
        RateLimitProperties properties = properties(true, 1, 1, 60, false);
        RateLimitWebFilter filter = new RateLimitWebFilter(properties, new RateLimitBucketService(properties));

        MockServerWebExchange firstPath = exchange(HttpMethod.GET, "/api/agendamentos/1", "10.0.0.1");
        MockServerWebExchange secondPath = exchange(HttpMethod.GET, "/api/bloqueios/1", "10.0.0.1");
        TrackingWebFilterChain firstChain = new TrackingWebFilterChain();
        TrackingWebFilterChain secondChain = new TrackingWebFilterChain();

        filter.filter(firstPath, firstChain).block();
        filter.filter(secondPath, secondChain).block();

        assertTrue(firstChain.wasCalled());
        assertFalse(secondChain.wasCalled());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, secondPath.getResponse().getStatusCode());
    }

    private static RateLimitProperties properties(
        boolean enabled,
        long capacity,
        long refillTokens,
        long refillDurationSeconds,
        boolean includePathInKey
    ) {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(enabled);
        properties.setCapacity(capacity);
        properties.setRefillTokens(refillTokens);
        properties.setRefillDurationSeconds(refillDurationSeconds);
        properties.setIncludePathInKey(includePathInKey);
        return properties;
    }

    private static MockServerWebExchange exchange(HttpMethod method, String path, String ip) {
        MockServerHttpRequest request = MockServerHttpRequest.method(method, path)
            .remoteAddress(new InetSocketAddress(ip, 8080))
            .build();
        return MockServerWebExchange.from(request);
    }

    private static final class TrackingWebFilterChain implements WebFilterChain {

        private boolean called;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.called = true;
            return Mono.empty();
        }

        boolean wasCalled() {
            return called;
        }
    }
}
