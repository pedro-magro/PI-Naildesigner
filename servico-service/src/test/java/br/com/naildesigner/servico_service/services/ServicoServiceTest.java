package br.com.naildesigner.servico_service.services;

import br.com.naildesigner.servico_service.dtos.ResponseServicoDTO;
import br.com.naildesigner.servico_service.models.Servico;
import br.com.naildesigner.servico_service.repositories.ServicoRepository;
import br.com.naildesigner.servico_service.services.ServicoService;
import br.com.naildesigner.servico_service.dtos.RequestServicoDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    RequestServicoDTO dtoValida;
    RequestServicoDTO dtoInvalida;

    @BeforeEach
    public void setup() {

        dtoValida = new RequestServicoDTO("Servico", "Descricao", 10.0, 10, List.of("image1.jpg", "image2.jpg"));
        dtoInvalida = new RequestServicoDTO(null, null, -50.0, -30, null);

    }

    @Test
    public void ct115_deveRetornarTrueQuandoInputValido(){
        boolean precoValidado = servicoService.validarInput(dtoValida);
        assertTrue(precoValidado);

    }

    @Test
    public void ct114_deveRetornarFalseQuandoInputInvalido(){
        boolean precoValidado = servicoService.validarInput(dtoInvalida);
        assertFalse(precoValidado);
    }

    @Test
    public void ct116_deveSalvarServicoComSucesso() {
        when(servicoRepository.save(any(Servico.class)))
                .thenAnswer(invocation -> {
                    Servico servico = invocation.getArgument(0);
                    servico.setId(1L);
                    return servico;
                });

        ResponseServicoDTO resultado = servicoService.salvar(dtoValida);

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        verify(servicoRepository).save(captor.capture());

        Servico salvo = captor.getValue();
        assertEquals("Servico", salvo.getNome());
        assertEquals("Descricao", salvo.getDescricao());
        assertEquals(10.0, salvo.getPreco());
        assertEquals(10, salvo.getDuracao());
        assertEquals(List.of("image1.jpg", "image2.jpg"), salvo.getImagens());

        assertEquals(1L, resultado.id());
        assertEquals("Servico", resultado.nome());
    }

    @Test
    public void ct112_deveLancarExceptionQuandoInputInvalido(){
        assertThrows(IllegalArgumentException.class, () ->
                servicoService.salvar(dtoInvalida));

        verify(servicoRepository, never()).save(any());
    }

    @Test
    public void ct113_deveListarTodosOsServicos() {
        Servico servico = new Servico();
        servico.setId(1L);
        servico.setNome(dtoValida.nome());
        servico.setDescricao(dtoValida.descricao());
        servico.setPreco(dtoValida.preco());
        servico.setDuracao(dtoValida.duracao());
        servico.setImagens(dtoValida.imagens());

        when(servicoRepository.findAll()).thenReturn(List.of(servico));

        List<ResponseServicoDTO> resultado = servicoService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).id());
        assertEquals(dtoValida.nome(), resultado.get(0).nome());
    }

    @Test
    public void ct108_deveBuscarServicoPorIdComSucesso() {
        Servico servico = new Servico();
        servico.setId(1L);
        servico.setNome(dtoValida.nome());
        servico.setDescricao(dtoValida.descricao());
        servico.setPreco(dtoValida.preco());
        servico.setDuracao(dtoValida.duracao());
        servico.setImagens(dtoValida.imagens());

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        ResponseServicoDTO resultado = servicoService.buscarPorId(1L);

        assertEquals(1L, resultado.id());
        assertEquals(dtoValida.nome(), resultado.nome());
        assertEquals(dtoValida.descricao(), resultado.descricao());
    }

    @Test
    public void ct111_deveLancarExcecaoQuandoServicoNaoForEncontrado() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                servicoService.buscarPorId(1L)
        );
    }

    @Test
    public void ct107_deveAtualizarServicoComSucesso() {
        Servico existente = new Servico();
        existente.setId(1L);
        existente.setNome("Antigo");
        existente.setDescricao("Descricao antiga");
        existente.setPreco(5.0);
        existente.setDuracao(5);
        existente.setImagens(List.of("old.jpg"));

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(servicoRepository.save(any(Servico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseServicoDTO resultado = servicoService.atualizar(1L, dtoValida);

        assertEquals(dtoValida.nome(), resultado.nome());
        assertEquals(dtoValida.descricao(), resultado.descricao());
        assertEquals(dtoValida.preco(), resultado.preco());
        assertEquals(dtoValida.duracao(), resultado.duracao());
        assertEquals(dtoValida.imagens(), resultado.imagens());
    }

    @Test
    public void ct110_deveLancarExcecaoAoAtualizarServicoInexistente() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                servicoService.atualizar(1L, dtoValida)
        );

        verify(servicoRepository, never()).save(any());
    }

    @Test
    public void ct109_deveExcluirServicoPorId(){
        servicoService.excluir(1L);
        verify(servicoRepository).deleteById(1L);
    }

}
