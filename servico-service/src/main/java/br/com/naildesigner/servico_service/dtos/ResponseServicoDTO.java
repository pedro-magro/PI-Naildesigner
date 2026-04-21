package br.com.naildesigner.servico_service.dtos;


import java.util.List;

public record ResponseServicoDTO(Long id, String nome, String descricao, Double preco, Integer duracao, List<String> imagens) {
}
