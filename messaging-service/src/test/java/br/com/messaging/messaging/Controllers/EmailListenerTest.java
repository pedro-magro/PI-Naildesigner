package br.com.messaging.messaging.Controllers;

import br.com.messaging.messaging.Services.EmailService;
import br.com.nailDesigner.messaging.api.dto.EmailDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailListenerTest {

    @Mock
    private EmailService emailService;

    @Test
    void ct94_deveEncaminharEmailConsumidoParaServicoDeEnvio() {
        EmailListener listener = new EmailListener(emailService);
        EmailDto emailDto = new EmailDto(List.of("cliente@nail.com"), "Assunto", "Corpo");

        listener.receiveEmail(emailDto);

        verify(emailService).sendEmail(emailDto.to(), emailDto.subject(), emailDto.body());
    }
}
