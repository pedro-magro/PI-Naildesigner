package br.com.nailDesigner.auth.api.Dtos;

//DTO para receber os dados de atualização do perfil.
//Os campos são opcionais, o usuário pode querer mudar apenas o telefone, por exemplo.
public record UpdateProfileRequestDto(
String username,
String phone,
String password // A nova senha, se o usuário quiser mudar
) {
	
}
