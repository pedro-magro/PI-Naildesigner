package br.com.naildesigner.agendamento_service_AgendeNail.services;

import br.com.naildesigner.agendamento_service_AgendeNail.dtos.BloqueioHorarioDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.models.BloqueioHorario;
import br.com.naildesigner.agendamento_service_AgendeNail.repositories.BloqueioHorarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class BloqueioHorarioServiceTest {

    @Mock
    private BloqueioHorarioRepository bloqueioDeHorarioRepository;

    @InjectMocks
    private BloqueioHorarioService bloqueioDeHorarioService;

    private UUID profissionalId;
    private LocalDate data;
    private BloqueioHorarioDTO dto;

    @BeforeEach
    public void setUp() {
        profissionalId = UUID.randomUUID();
        data = LocalDate.of(2026, 10, 1);

        dto = new BloqueioHorarioDTO();
        dto.setProfissionalId(profissionalId);
        dto.setDataBloqueio(data);
        dto.setHoraInicio(LocalTime.of(10, 0));
        dto.setHoraFim(LocalTime.of(12, 0));
        dto.setMotivo("Consulta Médica");

    }

    @Test
    public void ct90_deveSalvarBloqueioHorarioComSucesso(){
        when(bloqueioDeHorarioRepository
                .findByProfissionalIdAndDataBloqueioAndHoraFimAfterAndHoraInicioBefore(
                        profissionalId, data, LocalTime.of(10, 0), LocalTime.of(12, 0)))
          .thenReturn(List.of());

        when(bloqueioDeHorarioRepository.save(any(BloqueioHorario.class)))
                .thenAnswer(invocation -> {
                    BloqueioHorario bloqueio = invocation.getArgument(0);
                    bloqueio.setId(1L);
                    return bloqueio;
                });

        BloqueioHorarioDTO resultado = bloqueioDeHorarioService.salvarBloqueioHorario(dto);

        ArgumentCaptor<BloqueioHorario> captor = ArgumentCaptor.forClass(BloqueioHorario.class);
        verify(bloqueioDeHorarioRepository).save(captor.capture());

        BloqueioHorario salvo = captor.getValue();
        assertEquals(profissionalId, salvo.getProfissionalId());
        assertEquals(data, salvo.getDataBloqueio());
        assertEquals(LocalTime.of(10, 0), salvo.getHoraInicio());
        assertEquals(LocalTime.of(12, 0), salvo.getHoraFim());
        assertEquals("Consulta Médica", salvo.getMotivo());

        assertEquals(1L, resultado.getId());
        assertEquals(profissionalId, resultado.getProfissionalId());

    }

    @Test
    public void ct93_naoDeveSalvarQuandoHoraFimForAnteriorHoraInicio(){
        dto.setHoraInicio(LocalTime.of(14, 0));
        dto.setHoraFim(LocalTime.of(12, 0));

        assertThrows(IllegalArgumentException.class, () ->
                bloqueioDeHorarioService.salvarBloqueioHorario(dto));
        verify(bloqueioDeHorarioRepository, never()).save(any());
    }

    @Test
    public void ct92_naoDeveSalvarQuandoHaConflitoDeBloqueios(){
        BloqueioHorario conflito = new BloqueioHorario();
        conflito.setId(99L);

        when(bloqueioDeHorarioRepository
                .findByProfissionalIdAndDataBloqueioAndHoraFimAfterAndHoraInicioBefore(
                        profissionalId, data, LocalTime.of(10, 0), LocalTime.of(12, 0)))
                .thenReturn(List.of(conflito));

        assertThrows(IllegalArgumentException.class, () ->
                bloqueioDeHorarioService.salvarBloqueioHorario(dto));
        verify(bloqueioDeHorarioRepository, never()).save(any());
    }

    @Test
    public void ct82_devePermitirBloqueioDeDiaInteiro(){
        dto.setHoraInicio(null);
        dto.setHoraFim(null);

        when(bloqueioDeHorarioRepository.save(any(BloqueioHorario.class)))
                .thenAnswer(invocation -> {
                    BloqueioHorario bloqueio = invocation.getArgument(0);
                    bloqueio.setId(1L);
                    return bloqueio;
                });
        BloqueioHorarioDTO resultado = bloqueioDeHorarioService.salvarBloqueioHorario(dto);
        verify(bloqueioDeHorarioRepository).save(any(BloqueioHorario.class));
        assertEquals(1L, resultado.getId());
        assertNull(resultado.getHoraInicio());
        assertNull(resultado.getHoraFim());
    }

    @Test
    public void ct76_deveBuscarBloqueioPorIdComSucesso(){
        BloqueioHorario bloqueio = new BloqueioHorario();
        bloqueio.setId(1L);
        bloqueio.setProfissionalId(profissionalId);
        bloqueio.setDataBloqueio(data);
        bloqueio.setHoraInicio(LocalTime.of(10, 0));
        bloqueio.setHoraFim(LocalTime.of(12, 0));
        bloqueio.setMotivo("Consulta Médica");
        when(bloqueioDeHorarioRepository.findById(1L)).thenReturn(Optional.of(bloqueio));

        BloqueioHorarioDTO resultado = bloqueioDeHorarioService.buscarBloqueioHorarioPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals(profissionalId, resultado.getProfissionalId());
        assertEquals(data, resultado.getDataBloqueio());
        assertEquals(LocalTime.of(10, 0), resultado.getHoraInicio());
        assertEquals(LocalTime.of(12, 0), resultado.getHoraFim());
        assertEquals("Consulta Médica", resultado.getMotivo());
    }

    @Test
    public void ct80_deveLancarExcecaoQuandoBloqueioNaoExistir(){
        when(bloqueioDeHorarioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                bloqueioDeHorarioService.buscarBloqueioHorarioPorId(1L));
    }

    @Test
    public void ct75_deveAtualizarBloqueioComSucesso() {
        BloqueioHorario existente = new BloqueioHorario();
        existente.setId(1L);
        existente.setProfissionalId(profissionalId);
        existente.setDataBloqueio(data);
        existente.setHoraInicio(LocalTime.of(8, 0));
        existente.setHoraFim(LocalTime.of(9, 0));
        existente.setMotivo("Antigo");

        when(bloqueioDeHorarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(bloqueioDeHorarioRepository
                .findByProfissionalIdAndDataBloqueioAndHoraFimAfterAndHoraInicioBeforeAndIdIsNot(
                        profissionalId, data, LocalTime.of(10, 0), LocalTime.of(12, 0), 1L))
                .thenReturn(List.of());
        when(bloqueioDeHorarioRepository.save(any(BloqueioHorario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BloqueioHorarioDTO resultado = bloqueioDeHorarioService.atualizarBloqueioHorario(1L, dto);

        assertEquals(LocalTime.of(10, 0), resultado.getHoraInicio());
        assertEquals(LocalTime.of(12, 0), resultado.getHoraFim());
        assertEquals("Consulta Médica", resultado.getMotivo());
    }

    @Test
    public void ct78_deveFalharAoAtualizarBloqueioInexistente(){
        when(bloqueioDeHorarioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                bloqueioDeHorarioService.atualizarBloqueioHorario(1L, dto));
        verify(bloqueioDeHorarioRepository, never()).save(any());
    }

    @Test
    public void ct91_naoDeveAtualizarQuandoHouverConflito(){
        BloqueioHorario existente = new BloqueioHorario();
        existente.setId(1L);

        when(bloqueioDeHorarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(bloqueioDeHorarioRepository
                .findByProfissionalIdAndDataBloqueioAndHoraFimAfterAndHoraInicioBeforeAndIdIsNot(
                        profissionalId, data, LocalTime.of(10, 0), LocalTime.of(12, 0), 1L))
                .thenReturn(List.of(new BloqueioHorario()));

        assertThrows(IllegalArgumentException.class, () ->
                bloqueioDeHorarioService.atualizarBloqueioHorario(1L, dto));
        verify(bloqueioDeHorarioRepository, never()).save(any());

    }

    @Test
    public void ct77_deveDeletarBloqueioComSucesso(){
        when(bloqueioDeHorarioRepository.existsById(1L)).thenReturn(true);

        bloqueioDeHorarioService.excluirBloqueioHorario(1L);

        verify(bloqueioDeHorarioRepository).deleteById(1L);
    }

    @Test
    public void ct79_deveFalharAoDeletarBloqueioInexistente(){
        when(bloqueioDeHorarioRepository.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () ->
                bloqueioDeHorarioService.excluirBloqueioHorario(1L));
        verify(bloqueioDeHorarioRepository, never()).deleteById(anyLong());
    }

    @Test
    public void ct81_deveListarTodosBloqueios() {
        BloqueioHorario bloqueio = new BloqueioHorario();
        bloqueio.setId(1L);
        bloqueio.setProfissionalId(profissionalId);
        bloqueio.setDataBloqueio(data);

        when(bloqueioDeHorarioRepository.findAll()).thenReturn(List.of(bloqueio));

        List<BloqueioHorarioDTO> resultado = bloqueioDeHorarioService.listarTodosBloqueiosHorarios();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    public void ct89_deveRetornarTrueQuandoNaoHouverBloqueiosNoDia() {
        LocalDateTime inicioAgendamento = LocalDateTime.of(2026, 5, 10, 10, 0);
        LocalDateTime fimAgendamento = LocalDateTime.of(2026, 5, 10, 11, 0);

        when(bloqueioDeHorarioRepository.findByProfissionalIdAndDataBloqueio(profissionalId, inicioAgendamento.toLocalDate()))
                .thenReturn(List.of());

        boolean disponivel = bloqueioDeHorarioService.isPeriodoDisponivelParaAgendamento(
                profissionalId, inicioAgendamento, fimAgendamento, null
        );

        assertTrue(disponivel);
    }

    @Test
    public void ct86_deveRetornarFalseQuandoHouverSobreposicaoParcial() {
        BloqueioHorario bloqueio = new BloqueioHorario();
        bloqueio.setProfissionalId(profissionalId);
        bloqueio.setDataBloqueio(data);
        bloqueio.setHoraInicio(LocalTime.of(10, 0));
        bloqueio.setHoraFim(LocalTime.of(12, 0));

        when(bloqueioDeHorarioRepository.findByProfissionalIdAndDataBloqueio(profissionalId, data))
                .thenReturn(List.of(bloqueio));

        boolean disponivel = bloqueioDeHorarioService.isPeriodoDisponivelParaAgendamento(
                profissionalId,
                LocalDateTime.of(2026, 10, 1, 11, 0),
                LocalDateTime.of(2026, 10, 1, 13, 0),
                null
        );

        assertFalse(disponivel);
    }

    @Test
    public void ct83_deveRetornarFalseQuandoAgendamentoEstiverContidoNoBloqueio() {
        BloqueioHorario bloqueio = new BloqueioHorario();
        bloqueio.setProfissionalId(profissionalId);
        bloqueio.setDataBloqueio(data);
        bloqueio.setHoraInicio(LocalTime.of(10, 0));
        bloqueio.setHoraFim(LocalTime.of(15, 0));

        when(bloqueioDeHorarioRepository.findByProfissionalIdAndDataBloqueio(profissionalId, data))
                .thenReturn(List.of(bloqueio));

        boolean disponivel = bloqueioDeHorarioService.isPeriodoDisponivelParaAgendamento(
                profissionalId,
                LocalDateTime.of(2026, 10, 1, 11, 0),
                LocalDateTime.of(2026, 10, 1, 12, 0),
                null
        );

        assertFalse(disponivel);
    }

    @Test
    public void ct84_deveRetornarFalseQuandoBloqueioEstiverContidoNoAgendamento() {
        BloqueioHorario bloqueio = new BloqueioHorario();
        bloqueio.setProfissionalId(profissionalId);
        bloqueio.setDataBloqueio(data);
        bloqueio.setHoraInicio(LocalTime.of(11, 0));
        bloqueio.setHoraFim(LocalTime.of(12, 0));

        when(bloqueioDeHorarioRepository.findByProfissionalIdAndDataBloqueio(profissionalId, data))
                .thenReturn(List.of(bloqueio));

        boolean disponivel = bloqueioDeHorarioService.isPeriodoDisponivelParaAgendamento(
                profissionalId,
                LocalDateTime.of(2026, 10, 1, 10, 0),
                LocalDateTime.of(2026, 10, 1, 13, 0),
                null
        );

        assertFalse(disponivel);
    }

    @Test
    public void ct87_deveRetornarTrueQuandoAgendamentoComecarNoFimDoBloqueio() {
        BloqueioHorario bloqueio = new BloqueioHorario();
        bloqueio.setProfissionalId(profissionalId);
        bloqueio.setDataBloqueio(data);
        bloqueio.setHoraInicio(LocalTime.of(10, 0));
        bloqueio.setHoraFim(LocalTime.of(12, 0));

        when(bloqueioDeHorarioRepository.findByProfissionalIdAndDataBloqueio(profissionalId, data))
                .thenReturn(List.of(bloqueio));

        boolean disponivel = bloqueioDeHorarioService.isPeriodoDisponivelParaAgendamento(
                profissionalId,
                LocalDateTime.of(2026, 10, 1, 12, 0),
                LocalDateTime.of(2026, 10, 1, 13, 0),
                null
        );

        assertTrue(disponivel);
    }

    @Test
    public void ct88_deveRetornarTrueQuandoAgendamentoTerminarNoInicioDoBloqueio() {
        BloqueioHorario bloqueio = new BloqueioHorario();
        bloqueio.setProfissionalId(profissionalId);
        bloqueio.setDataBloqueio(data);
        bloqueio.setHoraInicio(LocalTime.of(10, 0));
        bloqueio.setHoraFim(LocalTime.of(12, 0));

        when(bloqueioDeHorarioRepository.findByProfissionalIdAndDataBloqueio(profissionalId, data))
                .thenReturn(List.of(bloqueio));

        boolean disponivel = bloqueioDeHorarioService.isPeriodoDisponivelParaAgendamento(
                profissionalId,
                LocalDateTime.of(2026, 10, 1, 9, 0),
                LocalDateTime.of(2026, 10, 1, 10, 0),
                null
        );

        assertTrue(disponivel);
    }

    @Test
    public void ct85_deveRetornarFalseQuandoHouverBloqueioDeDiaInteiro() {
        BloqueioHorario bloqueio = new BloqueioHorario();
        bloqueio.setProfissionalId(profissionalId);
        bloqueio.setDataBloqueio(data);
        bloqueio.setHoraInicio(null);
        bloqueio.setHoraFim(null);

        when(bloqueioDeHorarioRepository.findByProfissionalIdAndDataBloqueio(profissionalId, data))
                .thenReturn(List.of(bloqueio));

        boolean disponivel = bloqueioDeHorarioService.isPeriodoDisponivelParaAgendamento(
                profissionalId,
                LocalDateTime.of(2026, 10, 1, 10, 0),
                LocalDateTime.of(2026, 10, 1, 11, 0),
                null
        );

        assertFalse(disponivel);
    }



}
