package br.com.naildesigner.agendamento_service_AgendeNail.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.retry.support.RetryTemplate;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void ct226_deveConfigurarRetryLimitadoParaPublicacaoRabbit() {
        RetryTemplate retryTemplate = config.rabbitRetryTemplate();
        AtomicInteger tentativas = new AtomicInteger();

        assertThrows(AmqpException.class, () -> retryTemplate.execute(context -> {
            tentativas.incrementAndGet();
            throw new AmqpException("rabbit indisponivel");
        }));

        assertEquals(3, tentativas.get());
    }

    @Test
    void ct234_naoDeveRetentarErrorsNaPublicacaoRabbit() {
        RetryTemplate retryTemplate = config.rabbitRetryTemplate();
        AtomicInteger tentativas = new AtomicInteger();

        assertThrows(AssertionError.class, () -> retryTemplate.execute(context -> {
            tentativas.incrementAndGet();
            throw new AssertionError("erro nao retryable");
        }));

        assertEquals(1, tentativas.get());
    }

    @Test
    void ct226_deveCriarConversorJson() {
        assertNotNull(config.rabbitMessageConverter());
    }
}
