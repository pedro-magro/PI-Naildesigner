package br.com.messaging.messaging.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(List<String> to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            
            //convertendo a lista para array 
            message.setTo(to.toArray(new String[0]));
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            System.out.println("Email enviado com sucesso para: " + String.join(", ", to));
        } catch (MailException e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            
        }
    }
}
