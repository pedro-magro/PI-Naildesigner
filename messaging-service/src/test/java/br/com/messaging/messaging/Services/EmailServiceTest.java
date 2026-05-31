package br.com.messaging.messaging.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestClient.Builder restClientBuilder;
    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        emailService = new EmailService(
            restClientBuilder,
            "api-key",
            "https://api.resend.com",
            "onboarding@resend.dev"
        );
    }

    @Test
    void deveIgnorarEnvioSemDestinatarios() {
        emailService.sendEmail(List.of(), "Assunto", "Corpo");

        verifyNoInteractions(restClient);
    }

    @Test
    void deveEnviarEmailQuandoPayloadForValido() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/emails")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.accepted().build());

        emailService.sendEmail(List.of("cliente@nail.com"), "Assunto", "Corpo");

        verify(restClient).post();
        verify(requestBodyUriSpec).uri("/emails");
        verify(requestBodySpec).retrieve();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void ct231_devePropagarErroTransitorioParaPermitirRetryDoListener() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/emails")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RestClientException("falha externa"));

        assertThrows(RestClientException.class,
            () -> emailService.sendEmail(List.of("cliente@nail.com"), "Assunto", "Corpo"));
    }

    @Test
    void ct232_deveClassificarStatus429ComoFalhaTransitoria() {
        RestClientResponseException exception = new RestClientResponseException(
            "rate limit",
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Too Many Requests",
            null,
            null,
            null
        );

        assertFalse(emailService.isDefinitiveFailure(exception));
    }

    @Test
    void ct233_deveClassificarErro4xxDiferenteDe429ComoFalhaDefinitiva() {
        RestClientResponseException exception = new RestClientResponseException(
            "unauthorized",
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            null,
            null,
            null
        );

        assertTrue(emailService.isDefinitiveFailure(exception));
    }

    @Test
    void ct233_deveConverterFalhaDefinitivaEmIllegalArgumentException() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/emails")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RestClientResponseException(
            "payload invalido",
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            null,
            null,
            null
        ));

        assertThrows(IllegalArgumentException.class,
            () -> emailService.sendEmail(List.of("cliente@nail.com"), "Assunto", "Corpo"));
    }
}
