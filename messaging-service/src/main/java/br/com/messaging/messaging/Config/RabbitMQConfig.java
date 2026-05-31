package br.com.messaging.messaging.Config;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${messaging.rabbitmq.queue}")
    private String queue;

    @Value("${messaging.rabbitmq.exchange}")
    private String exchange;

    @Value("${messaging.rabbitmq.routing-key}")
    private String routingKey;

    @Value("${messaging.rabbitmq.dlq:${messaging.rabbitmq.queue}.dlq}")
    private String deadLetterQueue;

    @Value("${messaging.rabbitmq.dlx:${messaging.rabbitmq.exchange}.dlx}")
    private String deadLetterExchange;

    @Value("${messaging.rabbitmq.dlq-routing-key:${messaging.rabbitmq.routing-key}.dlq}")
    private String deadLetterRoutingKey;

    @Bean
    public Queue emailQueue() {
        return new Queue(queue, true, false, false, Map.of(
            "x-dead-letter-exchange", deadLetterExchange,
            "x-dead-letter-routing-key", deadLetterRoutingKey
        ));
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Binding binding(Queue emailQueue, DirectExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(routingKey);
    }

    @Bean
    public Queue emailDeadLetterQueue() {
        return new Queue(deadLetterQueue, true);
    }

    @Bean
    public DirectExchange emailDeadLetterExchange() {
        return new DirectExchange(deadLetterExchange);
    }

    @Bean
    public Binding deadLetterBinding(Queue emailDeadLetterQueue, DirectExchange emailDeadLetterExchange) {
        return BindingBuilder.bind(emailDeadLetterQueue).to(emailDeadLetterExchange).with(deadLetterRoutingKey);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter rabbitMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory,
        MessageConverter rabbitMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(rabbitRetryInterceptor());
        return factory;
    }

    @Bean
    public Advice rabbitRetryInterceptor() {
        return RetryInterceptorBuilder.stateless()
            .retryOperations(listenerRetryTemplate())
            .recoverer(new RejectAndDontRequeueRecoverer())
            .build();
    }

    @Bean
    public RetryTemplate listenerRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
            3,
            Map.of(RestClientException.class, true),
            true,
            false
        );

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1_000L);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(4_000L);

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }
}
