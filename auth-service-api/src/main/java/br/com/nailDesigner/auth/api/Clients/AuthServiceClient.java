package br.com.nailDesigner.auth.api.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.nailDesigner.auth.api.Dtos.LoginRequest;
import br.com.nailDesigner.auth.api.Dtos.LoginResponse;
import br.com.nailDesigner.auth.api.Dtos.RegisterRequest;

@FeignClient(name = "auth-service", url = "${microservices.auth.url:}")
public interface AuthServiceClient {
	
	 @PostMapping("/auth/login")
	    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request);

	    /**
	     * Este método define o endpoint para registrar (cadastrar) um novo usuário.
	     */
	    @PostMapping("/auth/register")
	    ResponseEntity<String> register(@RequestBody RegisterRequest request);
}
