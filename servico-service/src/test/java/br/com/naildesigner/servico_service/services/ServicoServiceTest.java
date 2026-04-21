package br.com.naildesigner.servico_service.services;

import br.com.naildesigner.servico_service.services.ServicoService;
import br.com.naildesigner.servico_service.dtos.RequestServicoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServicoServiceTest {

    private ServicoService servicoService;
    RequestServicoDTO dtoValida;
    RequestServicoDTO dtoInvalida;
    @BeforeEach
    public void setup() {
        servicoService = new ServicoService();
        dtoValida = new RequestServicoDTO("Servico", "Descricao", 10.0, 10, List.of("image1.jpg", "image2.jpg"));
        dtoInvalida = new RequestServicoDTO(null, null, -50.0, -30, null);

    }

    @Test
    public void deveRetornarTrueQuandoInputValido(){
        boolean precoValidado = servicoService.validarInput(dtoValida);
        assertTrue(precoValidado);

    }

    @Test
    public void deveRetornarFalseQuandoInputInvalido(){
        boolean precoValidado = servicoService.validarInput(dtoInvalida);
        assertFalse(precoValidado);
    }



}
