package br.com.naildesigner.api_gateway.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled;
    private long capacity = 30;
    private long refillTokens = 30;
    private long refillDurationSeconds = 60;
    private boolean includePathInKey = true;
    private List<String> publicPaths = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getRefillTokens() {
        return refillTokens;
    }

    public void setRefillTokens(long refillTokens) {
        this.refillTokens = refillTokens;
    }

    public long getRefillDurationSeconds() {
        return refillDurationSeconds;
    }

    public void setRefillDurationSeconds(long refillDurationSeconds) {
        this.refillDurationSeconds = refillDurationSeconds;
    }

    public boolean isIncludePathInKey() {
        return includePathInKey;
    }

    public void setIncludePathInKey(boolean includePathInKey) {
        this.includePathInKey = includePathInKey;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }
}
