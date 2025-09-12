package br.com.nailDesigner.auth.api.Dtos;

import java.util.List; // Importar List

// Usar um record é uma forma moderna e concisa de criar DTOs imutáveis.
public record UserProfileDto(String username, String email, String phone, List<String> roles) {
    // Adicionamos o campo 'roles' aqui.
    // O construtor padrão de um record já lida com a inicialização.
}
