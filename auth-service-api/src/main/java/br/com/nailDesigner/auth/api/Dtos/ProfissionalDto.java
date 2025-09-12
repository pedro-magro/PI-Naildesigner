package br.com.nailDesigner.auth.api.Dtos;

import java.util.UUID;

//DTO para listar profissionais no front-end
public record ProfissionalDto(UUID id, String username) {
}
