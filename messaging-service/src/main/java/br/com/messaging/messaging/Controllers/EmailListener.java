package br.com.messaging.messaging.Controllers;

import br.com.messaging.messaging.Services.EmailService;
import br.com.nailDesigner.messaging.api.dto.EmailDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailListener {
    private final EmailService emailService;

    public EmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${messaging.rabbitmq.queue}")
    public void receiveEmail(EmailDto email) {
        emailService.sendEmail(email.to(), email.subject(), email.body());
    }
}

