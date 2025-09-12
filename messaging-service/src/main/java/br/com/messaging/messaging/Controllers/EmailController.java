package br.com.messaging.messaging.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.messaging.messaging.Services.EmailService;
import br.com.nailDesigner.messaging.api.dto.EmailDto;

@RestController
@RequestMapping("/emails")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailDto emailDto) {
        emailService.sendEmail(emailDto.to(), emailDto.subject(), emailDto.body());
        return ResponseEntity.ok("Solicitação de envio de e-mails recebida.");
    }
}
