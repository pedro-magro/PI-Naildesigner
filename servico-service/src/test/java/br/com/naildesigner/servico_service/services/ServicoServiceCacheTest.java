package br.com.naildesigner.servico_service.services;

import br.com.naildesigner.servico_service.config.CacheConfig;
import br.com.naildesigner.servico_service.dtos.RequestServicoDTO;
import br.com.naildesigner.servico_service.models.Servico;
import br.com.naildesigner.servico_service.repositories.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = {
    ServicoService.class,
    CacheConfig.class,
    ServicoServiceCacheTest.TestConfig.class
})
class ServicoServiceCacheTest {

    @Autowired
    private ServicoService servicoService;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(servicoRepository);
        clearCache("servicos");
        clearCache("servicoPorId");
    }

    @Test
    void ct236_deveCachearListagemDeServicos() {
        when(servicoRepository.findAll()).thenReturn(List.of(servico(1L, "Servico")));

        servicoService.listarTodos();
        servicoService.listarTodos();

        verify(servicoRepository, times(1)).findAll();
    }

    @Test
    void ct237_deveCachearBuscaDeServicoPorId() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico(1L, "Servico")));

        servicoService.buscarPorId(1L);
        servicoService.buscarPorId(1L);

        verify(servicoRepository, times(1)).findById(1L);
    }

    @Test
    void ct238_deveInvalidarCacheAoCriarServico() {
        when(servicoRepository.findAll())
            .thenReturn(List.of(servico(1L, "Antigo")))
            .thenReturn(List.of(servico(1L, "Antigo"), servico(2L, "Novo")));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(invocation -> {
            Servico salvo = invocation.getArgument(0);
            salvo.setId(2L);
            return salvo;
        });

        servicoService.listarTodos();
        servicoService.salvar(new RequestServicoDTO("Novo", "Descricao", 10.0, 30, List.of("nova.jpg")));
        servicoService.listarTodos();

        verify(servicoRepository, times(2)).findAll();
    }

    @Test
    void ct239_deveInvalidarCacheAoAtualizarServico() {
        Servico antigo = servico(1L, "Antigo");
        Servico atualizado = servico(1L, "Atualizado");

        when(servicoRepository.findById(1L))
            .thenReturn(Optional.of(antigo))
            .thenReturn(Optional.of(antigo))
            .thenReturn(Optional.of(atualizado));
        when(servicoRepository.save(any(Servico.class))).thenReturn(atualizado);

        servicoService.buscarPorId(1L);
        servicoService.atualizar(1L, new RequestServicoDTO("Atualizado", "Descricao", 20.0, 40, List.of("nova.jpg")));
        servicoService.buscarPorId(1L);

        verify(servicoRepository, times(3)).findById(1L);
    }

    @Test
    void ct240_deveInvalidarCacheAoExcluirServico() {
        when(servicoRepository.findById(1L))
            .thenReturn(Optional.of(servico(1L, "Servico")))
            .thenReturn(Optional.empty());

        servicoService.buscarPorId(1L);
        servicoService.excluir(1L);

        assertThrows(EntityNotFoundException.class, () -> servicoService.buscarPorId(1L));
        verify(servicoRepository, times(2)).findById(1L);
    }

    @Test
    void ct237A_deveManterCacheSeparadoPorId() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico(1L, "Servico 1")));
        when(servicoRepository.findById(2L)).thenReturn(Optional.of(servico(2L, "Servico 2")));

        servicoService.buscarPorId(1L);
        servicoService.buscarPorId(2L);
        servicoService.buscarPorId(1L);

        verify(servicoRepository, times(1)).findById(1L);
        verify(servicoRepository, times(1)).findById(2L);
    }

    private void clearCache(String cacheName) {
        if (cacheManager.getCache(cacheName) != null) {
            cacheManager.getCache(cacheName).clear();
        }
    }

    private Servico servico(Long id, String nome) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setNome(nome);
        servico.setDescricao("Descricao");
        servico.setPreco(10.0);
        servico.setDuracao(30);
        servico.setImagens(List.of("imagem.jpg"));
        return servico;
    }

    @Configuration
    static class TestConfig {
        @Bean
        ServicoRepository servicoRepository() {
            return mock(ServicoRepository.class);
        }
    }
}
