package br.com.naildesigner.agendamento_service_AgendeNail.repositories;

import br.com.naildesigner.agendamento_service_AgendeNail.models.BloqueioHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BloqueioHorarioRepository extends JpaRepository<BloqueioHorario, Long> {

    // Método para encontrar bloqueios para uma data específica (já existia)
    List<BloqueioHorario> findByDataBloqueio(LocalDate dataBloqueio);

    // NOVO MÉTODO: Para encontrar bloqueios para uma profissional em uma data específica
    List<BloqueioHorario> findByProfissionalIdAndDataBloqueio(UUID profissionalId, LocalDate dataBloqueio);

    // NOVO MÉTODO: Para verificar bloqueios que se sobreponham a um período para uma profissional
    List<BloqueioHorario> findByProfissionalIdAndDataBloqueioAndHoraFimAfterAndHoraInicioBefore(
        UUID profissionalId,
        LocalDate dataBloqueio,
        LocalTime novoInicio,
        LocalTime novoFim
    );

    // NOVO MÉTODO: Para verificar bloqueios que se sobreponham a um período para uma profissional, excluindo o próprio
    List<BloqueioHorario> findByProfissionalIdAndDataBloqueioAndHoraFimAfterAndHoraInicioBeforeAndIdIsNot(
        UUID profissionalId,
        LocalDate dataBloqueio,
        LocalTime novoInicio,
        LocalTime novoFim,
        Long bloqueioIdExcluir
    );
}