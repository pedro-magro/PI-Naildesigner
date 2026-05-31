package br.com.messaging.messaging.Config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RabbitMQConfigTest {

    private RabbitMQConfig config;

    @BeforeEach
    void setUp() {
        config = new RabbitMQConfig();
        ReflectionTestUtils.setField(config, "queue", "email.queue");
        ReflectionTestUtils.setField(config, "exchange", "email.exchange");
        ReflectionTestUtils.setField(config, "routingKey", "email.routing.key");
        ReflectionTestUtils.setField(config, "deadLetterQueue", "email.queue.dlq");
        ReflectionTestUtils.setField(config, "deadLetterExchange", "email.exchange.dlx");
        ReflectionTestUtils.setField(config, "deadLetterRoutingKey", "email.routing.key.dlq");
    }

    @Test
    void ct228_deveConfigurarRetryLimitadoParaListenerRabbit() {
        RetryTemplate retryTemplate = config.listenerRetryTemplate();
        AtomicInteger tentativas = new AtomicInteger();

        assertThrows(RestClientException.class, () -> retryTemplate.execute(context -> {
            tentativas.incrementAndGet();
            throw new RestClientException("provider indisponivel");
        }));

        assertEquals(3, tentativas.get());
    }

    @Test
    void ct230_deveDeclararDlqEConfigurarFilaPrincipalComDeadLetter() {
        Queue queue = config.emailQueue();
        Queue dlq = config.emailDeadLetterQueue();

        assertEquals("email.queue", queue.getName());
        assertEquals("email.exchange.dlx", queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals("email.routing.key.dlq", queue.getArguments().get("x-dead-letter-routing-key"));
        assertEquals("email.queue.dlq", dlq.getName());
    }

    @Test
    void ct234B_naoDeveRetentarErrorsNoListenerRabbit() {
        RetryTemplate retryTemplate = config.listenerRetryTemplate();
        AtomicInteger tentativas = new AtomicInteger();

        assertThrows(AssertionError.class, () -> retryTemplate.execute(context -> {
            tentativas.incrementAndGet();
            throw new AssertionError("erro nao retryable");
        }));

        assertEquals(1, tentativas.get());
    }

    @Test
    void ct228A_deveCriarInterceptorDeRetryDoListener() {
        assertNotNull(config.rabbitRetryInterceptor());
    }
}
