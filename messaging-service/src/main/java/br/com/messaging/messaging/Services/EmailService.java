package br.com.messaging.messaging.Services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;
    private final String fromEmail;

    public EmailService(
        RestClient.Builder restClientBuilder,
        @Value("${resend.api-key:}") String resendApiKey,
        @Value("${resend.base-url:https://api.resend.com}") String resendBaseUrl,
        @Value("${resend.from-email:onboarding@resend.dev}") String fromEmail
    ) {
        this.restClient = restClientBuilder
            .baseUrl(resendBaseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        this.fromEmail = fromEmail;
    }

    public void sendEmail(List<String> to, String subject, String body) {
        if (to == null || to.isEmpty()) {
            logger.warn("Envio de email ignorado porque não há destinatários.");
            return;
        }

        try {
            restClient.post()
                .uri("/emails")
                .body(Map.of(
                    "from", fromEmail,
                    "to", to,
                    "subject", subject,
                    "text", body
                ))
                .retrieve()
                .toBodilessEntity();
            logger.info("Email enviado com sucesso para {} destinatário(s).", to.size());
        } catch (RestClientException e) {
            logger.error("Erro ao enviar email via Resend: {}", e.getMessage());
        }
    }
}
