package br.com.naildesigner.agendamento_service_AgendeNail.controllers;

import br.com.naildesigner.agendamento_service_AgendeNail.dtos.AgendamentoDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.dtos.AgendamentoRequestDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.dtos.DisponibilidadeDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.enums.AgendamentoStatus;
import br.com.naildesigner.agendamento_service_AgendeNail.services.AgendamentoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoControllerTest {

    @Mock
    private AgendamentoService agendamentoService;

    @InjectMocks
    private AgendamentoController agendamentoController;

    @Test
    void ct177_deveCriarAgendamentoComSucesso() {
        Authentication authentication = mock(Authentication.class);

        AgendamentoRequestDTO request = new AgendamentoRequestDTO(
                1L,
                UUID.randomUUID(),
                LocalDateTime.of(2026, 4, 10, 10, 0),
                "Observacao"
        );

        AgendamentoDTO dto = agendamentoDto(1L);

        when(agendamentoService.salvarAgendamento(request, authentication))
                .thenReturn(dto);

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.criarAgendamento(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());

        verify(agendamentoService).salvarAgendamento(request, authentication);
    }

    @Test
    void ct178_deveRetornarBadRequestAoCriarAgendamentoComErroDeNegocio() {
        Authentication authentication = mock(Authentication.class);

        AgendamentoRequestDTO request = new AgendamentoRequestDTO(
                1L,
                UUID.randomUUID(),
                LocalDateTime.of(2026, 4, 10, 10, 0),
                "Observacao"
        );

        when(agendamentoService.salvarAgendamento(request, authentication))
                .thenThrow(new IllegalArgumentException("horario invalido"));

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.criarAgendamento(request, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct179_deveBuscarDisponibilidade() {
        Authentication authentication = mock(Authentication.class);

        LocalDate data = LocalDate.of(2026, 4, 10);
        Long servicoId = 1L;
        UUID profissionalId = UUID.randomUUID();

        DisponibilidadeDTO disponibilidade =
                new DisponibilidadeDTO(data, List.of("09:00", "10:00"));

        when(agendamentoService.buscarHorariosDisponiveis(data, servicoId, profissionalId, authentication))
                .thenReturn(disponibilidade);

        ResponseEntity<DisponibilidadeDTO> response =
                agendamentoController.buscarDisponibilidade(data, servicoId, profissionalId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(disponibilidade, response.getBody());
    }

    @Test
    void ct180_deveListarMeusAgendamentosQuandoAuthenticationPossuirJwt() {
        UUID clienteId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(clienteId.toString())
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(jwt);

        List<AgendamentoDTO> agendamentos = List.of(agendamentoDto(1L));

        when(agendamentoService.listarAgendamentosPorCliente(clienteId))
                .thenReturn(agendamentos);

        ResponseEntity<List<AgendamentoDTO>> response =
                agendamentoController.listarMeusAgendamentos(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(agendamentos, response.getBody());

        verify(agendamentoService).listarAgendamentosPorCliente(clienteId);
    }

    @Test
    void ct181_deveRetornarUnauthorizedAoListarMeusAgendamentosSemJwt() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn("principal-invalido");

        ResponseEntity<List<AgendamentoDTO>> response =
                agendamentoController.listarMeusAgendamentos(authentication);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        verify(agendamentoService, never()).listarAgendamentosPorCliente(any(UUID.class));
    }

    @Test
    void ct182_deveCancelarAgendamentoComSucesso() throws IllegalAccessException {
        Authentication authentication = mock(Authentication.class);
        AgendamentoDTO dto = agendamentoDto(1L);
        dto.setStatus(AgendamentoStatus.CANCELADO);

        when(agendamentoService.cancelarAgendamento(1L, authentication))
                .thenReturn(dto);

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.cancelarAgendamento(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void ct183_deveRetornarUnauthorizedAoCancelarSemAuthentication() {
        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.cancelarAgendamento(1L, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct184_deveRetornarNotFoundAoCancelarAgendamentoInexistente() throws IllegalAccessException {
        Authentication authentication = mock(Authentication.class);

        when(agendamentoService.cancelarAgendamento(1L, authentication))
                .thenThrow(new EntityNotFoundException("nao encontrado"));

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.cancelarAgendamento(1L, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct185_deveRetornarForbiddenAoCancelarAgendamentoDeOutroUsuario() throws IllegalAccessException {
        Authentication authentication = mock(Authentication.class);

        when(agendamentoService.cancelarAgendamento(1L, authentication))
                .thenThrow(new IllegalAccessException("sem permissao"));

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.cancelarAgendamento(1L, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct186_deveRetornarBadRequestAoCancelarAgendamentoInvalido() throws IllegalAccessException {
        Authentication authentication = mock(Authentication.class);

        when(agendamentoService.cancelarAgendamento(1L, authentication))
                .thenThrow(new IllegalArgumentException("status invalido"));

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.cancelarAgendamento(1L, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct187_deveListarTodosAgendamentos() {
        List<AgendamentoDTO> agendamentos = List.of(agendamentoDto(1L), agendamentoDto(2L));

        when(agendamentoService.listarTodosAgendamentos())
                .thenReturn(agendamentos);

        ResponseEntity<List<AgendamentoDTO>> response =
                agendamentoController.listarTodosAgendamentos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(agendamentos, response.getBody());
    }

    @Test
    void ct188_deveBuscarAgendamentoPorIdExistente() {
        AgendamentoDTO dto = agendamentoDto(1L);

        when(agendamentoService.buscarAgendamentoPorId(1L))
                .thenReturn(dto);

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.buscarAgendamentoPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void ct189_deveRetornarNotFoundAoBuscarAgendamentoPorIdInexistente() {
        when(agendamentoService.buscarAgendamentoPorId(99L))
                .thenThrow(new EntityNotFoundException("nao encontrado"));

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.buscarAgendamentoPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct190_deveCriarAgendamentoPeloAdmin() {
        Authentication authentication = mock(Authentication.class);
        AgendamentoDTO request = agendamentoDto(null);
        AgendamentoDTO responseDto = agendamentoDto(1L);

        when(agendamentoService.salvarAgendamentoPeloAdmin(request, authentication))
                .thenReturn(responseDto);

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.criarAgendamentoPeloAdmin(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void ct191_deveRetornarBadRequestAoCriarAgendamentoPeloAdminComErroDeNegocio() {
        Authentication authentication = mock(Authentication.class);
        AgendamentoDTO request = agendamentoDto(null);

        when(agendamentoService.salvarAgendamentoPeloAdmin(request, authentication))
                .thenThrow(new IllegalArgumentException("conflito"));

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.criarAgendamentoPeloAdmin(request, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct192_deveAtualizarAgendamento() {
        Authentication authentication = mock(Authentication.class);
        AgendamentoDTO request = agendamentoDto(null);
        AgendamentoDTO atualizado = agendamentoDto(1L);

        when(agendamentoService.atualizarAgendamento(1L, request, authentication))
                .thenReturn(atualizado);

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.atualizarAgendamento(1L, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(atualizado, response.getBody());
    }

    @Test
    void ct193_deveRetornarNotFoundAoAtualizarAgendamentoInexistente() {
        Authentication authentication = mock(Authentication.class);
        AgendamentoDTO request = agendamentoDto(null);

        when(agendamentoService.atualizarAgendamento(99L, request, authentication))
                .thenThrow(new EntityNotFoundException("nao encontrado"));

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.atualizarAgendamento(99L, request, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct194_deveRetornarBadRequestAoAtualizarAgendamentoInvalido() {
        Authentication authentication = mock(Authentication.class);
        AgendamentoDTO request = agendamentoDto(null);

        when(agendamentoService.atualizarAgendamento(1L, request, authentication))
                .thenThrow(new IllegalArgumentException("conflito"));

        ResponseEntity<AgendamentoDTO> response =
                agendamentoController.atualizarAgendamento(1L, request, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct195_deveDeletarAgendamentoExistente() {
        ResponseEntity<Void> response =
                agendamentoController.deletarAgendamento(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(agendamentoService).excluirAgendamento(1L);
    }

    @Test
    void ct196_deveRetornarNotFoundAoDeletarAgendamentoInexistente() {
        doThrow(new EntityNotFoundException("nao encontrado"))
                .when(agendamentoService).excluirAgendamento(99L);

        ResponseEntity<Void> response =
                agendamentoController.deletarAgendamento(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    private AgendamentoDTO agendamentoDto(Long id) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setId(id);
        dto.setClienteId(UUID.randomUUID());
        dto.setProfissionalId(UUID.randomUUID());
        dto.setServicoId(1L);
        dto.setDataHoraInicio(LocalDateTime.of(2026, 4, 10, 10, 0));
        dto.setDataHoraFim(LocalDateTime.of(2026, 4, 10, 11, 0));
        dto.setStatus(AgendamentoStatus.CONFIRMADO);
        dto.setObservacoes("Teste");
        return dto;
    }
}