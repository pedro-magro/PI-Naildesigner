package br.com.naildesigner.agendamento_service_AgendeNail.services;

import br.com.nailDesigner.messaging.api.dto.EmailDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public EmailPublisher(RabbitTemplate rabbitTemplate,
                          @Value("${messaging.rabbitmq.exchange}") String exchange,
                          @Value("${messaging.rabbitmq.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishEmail(EmailDto email){
        rabbitTemplate.convertAndSend(exchange, routingKey, email);
    }

}
