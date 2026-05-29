package br.com.naildesigner.api_gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import br.com.naildesigner.api_gateway.config.RateLimitProperties;
import br.com.naildesigner.api_gateway.service.RateLimitBucketService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import reactor.core.publisher.Mono;

@Component
public class RateLimitWebFilter implements WebFilter, Ordered {

    private static final String TOO_MANY_REQUESTS_BODY =
        "{\"message\":\"Limite de requisições excedido. Tente novamente em instantes.\"}";

    private final RateLimitProperties properties;
    private final RateLimitBucketService bucketService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RateLimitWebFilter(RateLimitProperties properties, RateLimitBucketService bucketService) {
        this.properties = properties;
        this.bucketService = bucketService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (shouldSkip(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        String key = resolveKey(exchange);
        Bucket bucket = bucketService.resolveBucket(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            exchange.getResponse().getHeaders().set(
                "X-Rate-Limit-Remaining",
                String.valueOf(probe.getRemainingTokens())
            );
            return chain.filter(exchange);
        }

        return writeRateLimitedResponse(exchange.getResponse(), probe.getNanosToWaitForRefill());
    }

    private boolean shouldSkip(ServerHttpRequest request) {

        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return true;
        }

        return !properties.isEnabled();
    }

    private String resolveKey(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String clientIp = extractClientIp(request);

        if (!properties.isIncludePathInKey()) {
            return clientIp;
        }

        PathContainer path = request.getPath().pathWithinApplication();
        return clientIp + ":" + path.value();
    }

    private String extractClientIp(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown-client";
    }

    private Mono<Void> writeRateLimitedResponse(ServerHttpResponse response, long nanosToWait) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("Retry-After", String.valueOf(Math.max(1, nanosToWait / 1_000_000_000L)));

        byte[] payload = TOO_MANY_REQUESTS_BODY.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(payload)));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
