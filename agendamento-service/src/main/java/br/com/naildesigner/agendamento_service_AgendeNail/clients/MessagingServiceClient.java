package br.com.naildesigner.agendamento_service_AgendeNail.clients;

import br.com.naildesigner.agendamento_service_AgendeNail.clients.EmailRequestDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Cliente Feign para interagir com o Messaging Service.
 * 
 * O 'name' em @FeignClient DEVE corresponder ao spring.application.name
 * do messaging-service como registrado no Eureka.
 */
@FeignClient(name = "messaging-service")
public interface MessagingServiceClient {

    /**
     * Envia uma solicitação para o messaging-service para despachar um e-mail.
     *
     * O path aqui ("/emails/send") deve corresponder ao endpoint REAL no
     * EmailController do seu messaging-service.
     *
     * Exemplo de configuração esperada:
     * 
     * 1. API Gateway tem uma rota:
     *    Path=/api/messaging/**
     *    uri: lb://messaging-service
     *    filters:
     *      - StripPrefix=2  // Remove /api/messaging/
     *
     * 2. Messaging-service tem um EmailController:
     *    @RestController
     *    @RequestMapping("/emails") // ou apenas @RequestMapping("") se StripPrefix remover tudo até /send
     *    public class EmailController {
     *        @PostMapping("/send")
     *        public ResponseEntity<String> enviarEmail(@RequestBody EmailDto emailDto) { ... }
     *    }
     *
     * Com a configuração acima, o path no Feign client deve ser "/emails/send".
     * 
     * Se o EmailController no messaging-service estiver em @RequestMapping("/api/messaging/emails"),
     * e o Gateway NÃO tiver StripPrefix para essa parte, então o path aqui seria "/api/messaging/emails/send".
     * Ajuste conforme a sua estrutura real no messaging-service.
     *
     * @param emailRequest DTO contendo os dados do e-mail.
     * @param bearerToken  O token JWT completo (incluindo "Bearer ") para autenticação.
     * @return ResponseEntity<String> com a resposta do messaging-service.
     */
    @PostMapping("/emails/send") // Caminho do endpoint NO messaging-service
    ResponseEntity<String> sendEmail(
        @RequestBody EmailRequestDto emailRequest,
        @RequestHeader("Authorization") String bearerToken
    );
}
