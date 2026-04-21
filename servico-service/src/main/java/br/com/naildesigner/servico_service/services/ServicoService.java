package br.com.naildesigner.servico_service.services;

import br.com.naildesigner.servico_service.dtos.RequestServicoDTO;
import br.com.naildesigner.servico_service.dtos.ResponseServicoDTO;
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

    public ResponseServicoDTO salvar(RequestServicoDTO dto) {
        if(validarInput(dto)){

            Servico servico = new Servico();
            servico.setNome(sanitizarNome(dto.nome()));
            servico.setDescricao((sanitizarDescricao(dto.descricao())));
            servico.setPreco(sanitizarPreco(dto.preco()));
            servico.setImagens(sanitizarImagens(dto.imagens()));
            servico.setDuracao(dto.duracao());
            return toDTO(servicoRepo.save(servico));
        }
        else{
            throw new IllegalArgumentException("Dados inválidos");
        }

    }

    public List<ResponseServicoDTO> listarTodos() {
        return servicoRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ResponseServicoDTO buscarPorId(Long id) {
        Servico servico = servicoRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado com ID: " + id));
        return toDTO(servico);
    }

    public ResponseServicoDTO atualizar(Long id, RequestServicoDTO dto) {
        Servico servico = servicoRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado com ID: " + id));
        servico.setNome(dto.nome());
        servico.setDescricao(dto.descricao());
        servico.setPreco(dto.preco());
        servico.setImagens(dto.imagens());
        servico.setDuracao(dto.duracao());
        return toDTO(servicoRepo.save(servico));
    }

    public void excluir(Long id) {
        servicoRepo.deleteById(id);
    }

    private ResponseServicoDTO toDTO(Servico servico) {
        ResponseServicoDTO dto = new ResponseServicoDTO(servico.getId(), servico.getNome(), servico.getDescricao(), servico.getPreco(),servico.getDuracao(), servico.getImagens());
        return dto;
    }

    public boolean validarInput(RequestServicoDTO dto){
        if(!validarNome(dto.nome())) return false;
        if(!validarDescriçao(dto.descricao())) return false;
        if(!validarPreco(dto.preco()))return false;
        if(!validarDuracao(dto.duracao())) return false;

        return true;

    }

    private String sanitizarNome(String nome){
        if(nome == null) return null;
        return nome.trim()
                .replaceAll("<script>", "")
                .replaceAll("[<>]", "")
                .substring(0, Math.min(nome.length(), 100));
    }
    private String sanitizarDescricao(String desc){
        if(desc == null) return null;
        return desc.trim()
                .replaceAll("<script>", "")
                .replaceAll("[<>]", "")
                .substring(0, Math.min(desc.length(), 500));
    }

    private Double sanitizarPreco(Double preco){
        if(preco == null)return null;
        return Math.round(preco * 100.0) / 100.0;

    }

    private List<String> sanitizarImagens(List<String> imagens){
        if(imagens == null)return null;
        return imagens.stream()
                .filter(img -> img != null && !img.isBlank())
                .map(String::trim)
                .limit(10)
                .collect(Collectors.toList());
    }

    private boolean validarPreco(Double preco){
        return preco != null && preco > 0;
    }

    private boolean validarDuracao(Integer duracao){
        return duracao != null && duracao > 0;
    }
    private boolean validarDescriçao(String desc){
        return desc != null && !desc.trim().isEmpty();
    }
    private boolean validarNome(String nome){
        return nome != null && !nome.trim().isEmpty();
    }
}
