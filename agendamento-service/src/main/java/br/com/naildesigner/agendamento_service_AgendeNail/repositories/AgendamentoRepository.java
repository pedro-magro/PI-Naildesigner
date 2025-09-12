package br.com.naildesigner.agendamento_service_AgendeNail.repositories;

import br.com.naildesigner.agendamento_service_AgendeNail.models.Agendamento;
import br.com.naildesigner.agendamento_service_AgendeNail.enums.AgendamentoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Para conflitos ao SALVAR (não inclui o próprio ID, pois ainda não existe)
    // Condição: profissionalId = ? AND dataHoraFim > ? (dataHoraInicio do novo) 
    //           AND dataHoraInicio < ? (dataHoraFim do novo) AND status <> CANCELADO
    List<Agendamento> findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndStatusNot(
        UUID profissionalId,
        LocalDateTime dataHoraInicioReferencia,
        LocalDateTime dataHoraFimReferencia,
        AgendamentoStatus statusExcluido
    );

    // Para conflitos ao ATUALIZAR (exclui o próprio agendamento da verificação)
    // Condição: profissionalId = ? AND dataHoraFim > ? AND dataHoraInicio < ? 
    //           AND id <> ? AND status <> CANCELADO
    List<Agendamento> findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndIdNotAndStatusNot(
        UUID profissionalId,
        LocalDateTime dataHoraInicioReferencia,
        LocalDateTime dataHoraFimReferencia,
        Long agendamentoIdExcluido,
        AgendamentoStatus statusExcluido
    );

    // Para buscar agendamentos de um profissional em um dia específico, excluindo um status
    // Condição: profissionalId = ? AND dataHoraInicio BETWEEN ? AND ? AND status <> CANCELADO
    List<Agendamento> findByProfissionalIdAndDataHoraInicioBetweenAndStatusNot(
        UUID profissionalId,
        LocalDateTime inicioDoDia,
        LocalDateTime fimDoDia,
        AgendamentoStatus statusExcluido
    );

    // Para listar agendamentos por cliente
    List<Agendamento> findByClienteId(UUID clienteId);

    // Se precisar de outros métodos, você pode continuar a definir aqui.
}