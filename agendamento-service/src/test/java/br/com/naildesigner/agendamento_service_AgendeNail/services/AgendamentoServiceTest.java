package br.com.naildesigner.agendamento_service_AgendeNail.services;

import br.com.naildesigner.agendamento_service_AgendeNail.enums.AgendamentoStatus;
import br.com.naildesigner.agendamento_service_AgendeNail.models.Agendamento;
import br.com.naildesigner.agendamento_service_AgendeNail.repositories.AgendamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AgendamentoServiceTest {

    @Mock
    AgendamentoRepository agendamentoRepository;

    @InjectMocks
    AgendamentoService agendamentoService;

    private LocalDateTime inicio;
    private LocalDateTime fim;
    private UUID profissionalId;
    Long agendamentoId;

    @BeforeEach
    public void setUp() {
        inicio = LocalDateTime.of(2023, 10, 1, 10, 0);
        fim = LocalDateTime.of(2023, 10, 1, 10, 30);
        profissionalId = UUID.randomUUID();
        agendamentoId = 15L;
    }

    LocalDateTime dataHoraInicio = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime esperado = LocalDateTime.of(2023, 10, 1, 10, 30);

    @Test
    public void deveCalcularHorarioDeFimCorretamente(){
       LocalDateTime fimAgendamento = agendamentoService.calcularHorarioFim(dataHoraInicio, 30);

       assertEquals(esperado, fimAgendamento);
    }

    @Test
    public void deveValidarDisponibilidadeCorretamente(){
        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndIdNotAndStatusNot(profissionalId, inicio, fim, agendamentoId, AgendamentoStatus.CANCELADO)).thenReturn(new ArrayList<>());
        boolean disponivel = agendamentoService.validarDisponibilidade(profissionalId, inicio, fim, agendamentoId);
        assertTrue(disponivel);
    }

    @Test
    public void deveDarFalseQuandoNaoTiverDisponibilidade(){
        Agendamento agendamento = new Agendamento();
        agendamento.setProfissionalId(profissionalId);
        agendamento.setDataHoraInicio(inicio);
        agendamento.setDataHoraFim(fim);
        agendamento.setId(1L);
        agendamento.setStatus(AgendamentoStatus.PENDENTE);

        List<Agendamento> conflitos = new ArrayList<>();
        conflitos.add(agendamento);

        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndIdNotAndStatusNot(profissionalId, inicio, fim, agendamentoId, AgendamentoStatus.CANCELADO)).thenReturn(conflitos);

        assertFalse(agendamentoService.validarDisponibilidade(profissionalId, inicio, fim, agendamentoId));

    }
}
