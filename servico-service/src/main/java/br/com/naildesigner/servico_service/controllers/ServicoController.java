package br.com.naildesigner.servico_service.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import br.com.naildesigner.servico_service.dtos.ServicoDTO;
import br.com.naildesigner.servico_service.services.ServicoService;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    @Autowired
    private ServicoService servicoService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ServicoDTO salvar(@RequestBody ServicoDTO dto) {
        return servicoService.salvar(dto);
    }

    @GetMapping
    public List<ServicoDTO> listar() {
        return servicoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ServicoDTO buscarPorId(@PathVariable Long id) {
        return servicoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ServicoDTO atualizar(@PathVariable Long id, @RequestBody ServicoDTO dto) {
        dto.setId(id);
        return servicoService.atualizar(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void excluir(@PathVariable Long id) {
        servicoService.excluir(id);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Olá do Serviço de Serviços!";
    }
}
