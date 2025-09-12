package br.com.naildesigner.servico_service.services;

import br.com.naildesigner.servico_service.dtos.ServicoDTO;
import br.com.naildesigner.servico_service.models.Servico;
import br.com.naildesigner.servico_service.repositories.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicoService {

    @Autowired
    private ServicoRepository servicoRepo;

    public ServicoDTO salvar(ServicoDTO dto) {
        Servico servico = new Servico();
        servico.setNome(dto.getNome());
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        servico.setImagens(dto.getImagens());
        servico.setDuracao(dto.getDuracao());
        return toDTO(servicoRepo.save(servico));
    }

    public List<ServicoDTO> listarTodos() {
        return servicoRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ServicoDTO buscarPorId(Long id) {
        Servico servico = servicoRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado com ID: " + id));
        return toDTO(servico);
    }

    public ServicoDTO atualizar(ServicoDTO dto) {
        Servico servico = servicoRepo.findById(dto.getId())
            .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado com ID: " + dto.getId()));
        servico.setNome(dto.getNome());
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        servico.setImagens(dto.getImagens());
        servico.setDuracao(dto.getDuracao());
        return toDTO(servicoRepo.save(servico));
    }

    public void excluir(Long id) {
        servicoRepo.deleteById(id);
    }

    private ServicoDTO toDTO(Servico servico) {
        ServicoDTO dto = new ServicoDTO();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setDescricao(servico.getDescricao());
        dto.setPreco(servico.getPreco());
        dto.setImagens(servico.getImagens());
        dto.setDuracao(servico.getDuracao());
        return dto;
    }
}
