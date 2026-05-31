package br.com.naildesigner.api_gateway.service;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import br.com.naildesigner.api_gateway.config.RateLimitProperties;
import io.github.bucket4j.Bucket;

class RateLimitBucketServiceTest {

    @Test
    void ct100_deveReutilizarBucketParaMesmaChaveECriarNovoParaChaveDiferente() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setCapacity(5);
        properties.setRefillTokens(5);
        properties.setRefillDurationSeconds(60);

        RateLimitBucketService service = new RateLimitBucketService(properties);

        Bucket first = service.resolveBucket("client-1");
        Bucket second = service.resolveBucket("client-1");
        Bucket third = service.resolveBucket("client-2");

        assertSame(first, second);
        assertNotSame(first, third);
    }
}
