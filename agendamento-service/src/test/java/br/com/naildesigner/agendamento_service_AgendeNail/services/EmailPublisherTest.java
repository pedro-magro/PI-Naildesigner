package br.com.naildesigner.agendamento_service_AgendeNail.services;

import br.com.nailDesigner.messaging.api.dto.EmailDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EmailPublisher emailPublisher;

    @Test
    void ct95_devePublicarEmailNaExchangeConfigurada() {
        EmailDto emailDto = new EmailDto(List.of("cliente@nail.com"), "Assunto", "Corpo");
        ReflectionTestUtils.setField(emailPublisher, "exchange", "email.exchange");
        ReflectionTestUtils.setField(emailPublisher, "routingKey", "email.routing.key");

        emailPublisher.publishEmail(emailDto);

        verify(rabbitTemplate).convertAndSend("email.exchange", "email.routing.key", emailDto);
    }
}
