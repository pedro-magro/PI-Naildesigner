package br.com.naildesigner.agendamento_service_AgendeNail.controllers;

import br.com.naildesigner.agendamento_service_AgendeNail.dtos.BloqueioHorarioDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.services.BloqueioHorarioService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloqueioHorarioControllerTest {

    @Mock
    private BloqueioHorarioService bloqueioHorarioService;

    @InjectMocks
    private BloqueioHorarioController bloqueioHorarioController;

    @Test
    void ct197_deveCriarBloqueioComSucesso() {
        BloqueioHorarioDTO request = bloqueioDto(null);
        BloqueioHorarioDTO responseDto = bloqueioDto(1L);

        when(bloqueioHorarioService.salvarBloqueioHorario(request))
                .thenReturn(responseDto);

        ResponseEntity<BloqueioHorarioDTO> response =
                bloqueioHorarioController.criarBloqueio(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());

        verify(bloqueioHorarioService).salvarBloqueioHorario(request);
    }

    @Test
    void ct198_deveRetornarBadRequestAoCriarBloqueioInvalido() {
        BloqueioHorarioDTO request = bloqueioDto(null);

        when(bloqueioHorarioService.salvarBloqueioHorario(request))
                .thenThrow(new IllegalArgumentException("conflito"));

        ResponseEntity<BloqueioHorarioDTO> response =
                bloqueioHorarioController.criarBloqueio(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct199_deveListarBloqueios() {
        List<BloqueioHorarioDTO> bloqueios =
                List.of(bloqueioDto(1L), bloqueioDto(2L));

        when(bloqueioHorarioService.listarTodosBloqueiosHorarios())
                .thenReturn(bloqueios);

        ResponseEntity<List<BloqueioHorarioDTO>> response =
                bloqueioHorarioController.listarBloqueios();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bloqueios, response.getBody());

        verify(bloqueioHorarioService).listarTodosBloqueiosHorarios();
    }

    @Test
    void ct200_deveBuscarBloqueioPorIdExistente() {
        BloqueioHorarioDTO dto = bloqueioDto(1L);

        when(bloqueioHorarioService.buscarBloqueioHorarioPorId(1L))
                .thenReturn(dto);

        ResponseEntity<BloqueioHorarioDTO> response =
                bloqueioHorarioController.buscarBloqueioPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());

        verify(bloqueioHorarioService).buscarBloqueioHorarioPorId(1L);
    }

    @Test
    void ct201_deveRetornarNotFoundAoBuscarBloqueioInexistente() {
        when(bloqueioHorarioService.buscarBloqueioHorarioPorId(99L))
                .thenThrow(new EntityNotFoundException("nao encontrado"));

        ResponseEntity<BloqueioHorarioDTO> response =
                bloqueioHorarioController.buscarBloqueioPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct202_deveAtualizarBloqueioComSucesso() {
        BloqueioHorarioDTO request = bloqueioDto(null);
        BloqueioHorarioDTO atualizado = bloqueioDto(1L);

        when(bloqueioHorarioService.atualizarBloqueioHorario(1L, request))
                .thenReturn(atualizado);

        ResponseEntity<BloqueioHorarioDTO> response =
                bloqueioHorarioController.atualizarBloqueio(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(atualizado, response.getBody());

        verify(bloqueioHorarioService).atualizarBloqueioHorario(1L, request);
    }

    @Test
    void ct203_deveRetornarNotFoundAoAtualizarBloqueioInexistente() {
        BloqueioHorarioDTO request = bloqueioDto(null);

        when(bloqueioHorarioService.atualizarBloqueioHorario(99L, request))
                .thenThrow(new EntityNotFoundException("nao encontrado"));

        ResponseEntity<BloqueioHorarioDTO> response =
                bloqueioHorarioController.atualizarBloqueio(99L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct204_deveRetornarBadRequestAoAtualizarBloqueioInvalido() {
        BloqueioHorarioDTO request = bloqueioDto(null);

        when(bloqueioHorarioService.atualizarBloqueioHorario(1L, request))
                .thenThrow(new IllegalArgumentException("conflito"));

        ResponseEntity<BloqueioHorarioDTO> response =
                bloqueioHorarioController.atualizarBloqueio(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct205_deveExcluirBloqueioExistente() {
        ResponseEntity<Void> response =
                bloqueioHorarioController.excluirBloqueio(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(bloqueioHorarioService).excluirBloqueioHorario(1L);
    }

    @Test
    void ct206_deveRetornarNotFoundAoExcluirBloqueioInexistente() {
        doThrow(new EntityNotFoundException("nao encontrado"))
                .when(bloqueioHorarioService).excluirBloqueioHorario(99L);

        ResponseEntity<Void> response =
                bloqueioHorarioController.excluirBloqueio(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct207_deveBuscarBloqueiosPorProfissionalEData() {
        UUID profissionalId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2026, 4, 10);

        List<BloqueioHorarioDTO> bloqueios = List.of(bloqueioDto(1L));

        when(bloqueioHorarioService.buscarBloqueiosPorProfissionalEData(profissionalId, data))
                .thenReturn(bloqueios);

        ResponseEntity<List<BloqueioHorarioDTO>> response =
                bloqueioHorarioController.buscarBloqueiosPorProfissionalEData(profissionalId, data);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bloqueios, response.getBody());

        verify(bloqueioHorarioService).buscarBloqueiosPorProfissionalEData(profissionalId, data);
    }

    private BloqueioHorarioDTO bloqueioDto(Long id) {
        BloqueioHorarioDTO dto = new BloqueioHorarioDTO();
        dto.setId(id);
        dto.setProfissionalId(UUID.randomUUID());
        dto.setDataBloqueio(LocalDate.of(2026, 4, 10));
        dto.setHoraInicio(LocalTime.of(9, 0));
        dto.setHoraFim(LocalTime.of(12, 0));
        dto.setMotivo("Indisponivel");
        return dto;
    }
}