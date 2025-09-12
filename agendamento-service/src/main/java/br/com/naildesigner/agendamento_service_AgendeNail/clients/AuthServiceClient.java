package br.com.naildesigner.agendamento_service_AgendeNail.clients;

import br.com.naildesigner.agendamento_service_AgendeNail.config.FeignClientInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "auth-service", configuration = FeignClientInterceptor.class)
public interface AuthServiceClient {

	 @GetMapping("/auth/users/{id}/email")
    String getEmailPorId(@PathVariable("id") UUID id);
}