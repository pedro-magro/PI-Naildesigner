package br.com.nailDesigner.auth.api.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Username é obrigatório e deve ter no mínimo 3 caracteres") 
	    @Size(min = 3, message = "Username deve ter no mínimo 3 caracteres") 
	    String username,
	    
		@NotBlank(message = "Email é obrigatório e deve ser válido") 
	    @Email(message = "Email deve ser válido") 
	    String email,
	    
		@NotBlank (message = "Senha é obrigatória e deve ter no mínimo 6 caracteres")
		@Size(min = 6) String password,
		
		@NotBlank(message = "Telefone é obrigatório") 
	    String phone 
		) {}
