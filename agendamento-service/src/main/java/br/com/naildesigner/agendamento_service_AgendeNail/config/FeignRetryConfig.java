package br.com.naildesigner.agendamento_service_AgendeNail.config;

import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

import java.util.Set;

public class FeignRetryConfig {

    private static final Set<Integer> RETRYABLE_STATUS = Set.of(429, 500, 502, 503, 504);

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(500L, 2_000L, 3);
    }

    @Bean
    public ErrorDecoder feignErrorDecoder() {
        ErrorDecoder defaultDecoder = new ErrorDecoder.Default();
        return (methodKey, response) -> {
            if (isRetryable(response)) {
                Request request = response.request();
                return new RetryableException(
                    response.status(),
                    "Falha transitoria em chamada Feign: " + methodKey,
                    request.httpMethod(),
                    (Long) null,
                    request
                );
            }
            return defaultDecoder.decode(methodKey, response);
        };
    }

    public boolean isRetryable(Response response) {
        return response != null
            && response.request() != null
            && RETRYABLE_STATUS.contains(response.status());
    }
}
