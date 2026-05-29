package br.com.naildesigner.api_gateway.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import br.com.naildesigner.api_gateway.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class RateLimitBucketService {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitBucketService(RateLimitProperties properties) {
        this.properties = properties;
    }

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, ignored -> newBucket());
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.getCapacity())
                .refillIntervally(properties.getRefillTokens(), Duration.ofSeconds(properties.getRefillDurationSeconds()))
                .build();
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
