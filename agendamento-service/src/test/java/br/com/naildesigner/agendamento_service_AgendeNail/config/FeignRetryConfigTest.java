package br.com.naildesigner.agendamento_service_AgendeNail.config;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeignRetryConfigTest {

    private final FeignRetryConfig config = new FeignRetryConfig();

    @Test
    void ct223_deveTratarStatusTransitorioComoRetryable() {
        Response response = response(503);

        assertTrue(config.isRetryable(response));

        ErrorDecoder decoder = config.feignErrorDecoder();
        assertInstanceOf(RetryableException.class, decoder.decode("ServicoClient#getServicoById", response));
    }

    @Test
    void ct224_naoDeveTratarStatusDefinitivoComoRetryable() {
        Response response = response(404);

        assertFalse(config.isRetryable(response));
        assertFalse(config.feignErrorDecoder().decode("ServicoClient#getServicoById", response) instanceof RetryableException);
    }

    @Test
    void ct234_deveConfigurarRetryerLimitadoParaFeign() {
        assertNotNull(config.feignRetryer());
    }

    private Response response(int status) {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://servico-service/api/servicos/1",
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            new RequestTemplate()
        );

        return Response.builder()
            .status(status)
            .reason("status")
            .request(request)
            .build();
    }
}
