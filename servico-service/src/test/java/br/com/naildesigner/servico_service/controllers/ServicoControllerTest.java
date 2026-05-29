package br.com.naildesigner.servico_service.controllers;

import br.com.naildesigner.servico_service.dtos.RequestServicoDTO;
import br.com.naildesigner.servico_service.dtos.ResponseServicoDTO;
import br.com.naildesigner.servico_service.services.ServicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoControllerTest {

    @Mock
    private ServicoService servicoService;

    @InjectMocks
    private ServicoController servicoController;

    @Test
    void ct208_deveSalvarServico() {
        RequestServicoDTO request = requestDto();
        ResponseServicoDTO responseDto = responseDto(1L);

        when(servicoService.salvar(request))
                .thenReturn(responseDto);

        ResponseServicoDTO resultado =
                servicoController.salvar(request);

        assertEquals(responseDto, resultado);

        verify(servicoService).salvar(request);
    }

    @Test
    void ct209_deveListarServicos() {
        List<ResponseServicoDTO> servicos =
                List.of(responseDto(1L), responseDto(2L));

        when(servicoService.listarTodos())
                .thenReturn(servicos);

        List<ResponseServicoDTO> resultado =
                servicoController.listar();

        assertEquals(2, resultado.size());
        assertEquals(servicos, resultado);

        verify(servicoService).listarTodos();
    }

    @Test
    void ct210_deveBuscarServicoPorId() {
        ResponseServicoDTO responseDto = responseDto(1L);

        when(servicoService.buscarPorId(1L))
                .thenReturn(responseDto);

        ResponseServicoDTO resultado =
                servicoController.buscarPorId(1L);

        assertEquals(responseDto, resultado);

        verify(servicoService).buscarPorId(1L);
    }

    @Test
    void ct211_deveAtualizarServico() {
        RequestServicoDTO request = requestDto();
        ResponseServicoDTO responseDto = responseDto(1L);

        when(servicoService.atualizar(1L, request))
                .thenReturn(responseDto);

        ResponseServicoDTO resultado =
                servicoController.atualizar(1L, request);

        assertEquals(responseDto, resultado);

        verify(servicoService).atualizar(1L, request);
    }

    @Test
    void ct212_deveExcluirServico() {
        servicoController.excluir(1L);

        verify(servicoService).excluir(1L);
    }

    @Test
    void ct213_deveRetornarMensagemHello() {
        String resultado = servicoController.hello();

        assertEquals("Olá do Serviço de Serviços!", resultado);
    }

    private RequestServicoDTO requestDto() {
        return new RequestServicoDTO(
                "Manicure",
                "Servico de manicure",
                50.0,
                60,
                List.of("imagem-1.jpg")
        );
    }

    private ResponseServicoDTO responseDto(Long id) {
        return new ResponseServicoDTO(
                id,
                "Manicure",
                "Servico de manicure",
                50.0,
                60,
                List.of("imagem-1.jpg")
        );
    }
}