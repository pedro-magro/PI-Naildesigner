package br.com.naildesigner.agendamento_service_AgendeNail.services;

import java.time.LocalDate;
import java.time.LocalDateTime; // Adicionar import para LocalDateTime
import java.time.LocalTime;    // Adicionar import para LocalTime
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.naildesigner.agendamento_service_AgendeNail.dtos.BloqueioHorarioDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.models.BloqueioHorario;
import br.com.naildesigner.agendamento_service_AgendeNail.repositories.BloqueioHorarioRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class BloqueioHorarioService {

    @Autowired
    private BloqueioHorarioRepository bloqueioHorarioRepository;

    // --- Métodos CRUD para Bloqueio de Horários (EXISTENTES) ---
    @Transactional
    public BloqueioHorarioDTO salvarBloqueioHorario(BloqueioHorarioDTO dto) {
        if (dto.getHoraInicio() != null && dto.getHoraFim() != null && dto.getHoraInicio().isAfter(dto.getHoraFim())) {
            throw new IllegalArgumentException("A hora de fim do bloqueio não pode ser anterior à hora de início.");
        }
        if (dto.getHoraInicio() != null && dto.getHoraFim() != null) {
            List<BloqueioHorario> conflitos = bloqueioHorarioRepository.findByProfissionalIdAndDataBloqueioAndHoraFimAfterAndHoraInicioBefore(
                dto.getProfissionalId(),
                dto.getDataBloqueio(),
                dto.getHoraInicio(),
                dto.getHoraFim()
            );
            if (!conflitos.isEmpty()) {
                throw new IllegalArgumentException("Já existe um bloqueio para esta profissional neste período.");
            }
        }
        BloqueioHorario bloqueio = new BloqueioHorario();
        BeanUtils.copyProperties(dto, bloqueio, "id");
        bloqueio = bloqueioHorarioRepository.save(bloqueio);
        return toDTO(bloqueio);
    }

    public List<BloqueioHorarioDTO> listarTodosBloqueiosHorarios() {
        return bloqueioHorarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BloqueioHorarioDTO buscarBloqueioHorarioPorId(Long id) {
        BloqueioHorario bloqueio = bloqueioHorarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bloqueio de horário não encontrado com ID: " + id));
        return toDTO(bloqueio);
    }

    @Transactional
    public BloqueioHorarioDTO atualizarBloqueioHorario(Long id, BloqueioHorarioDTO dto) {
        BloqueioHorario bloqueioExistente = bloqueioHorarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bloqueio de horário não encontrado com ID: " + id));
        if (dto.getHoraInicio() != null && dto.getHoraFim() != null && dto.getHoraInicio().isAfter(dto.getHoraFim())) {
            throw new IllegalArgumentException("A hora de fim do bloqueio não pode ser anterior à hora de início.");
        }
        if (dto.getHoraInicio() != null && dto.getHoraFim() != null) {
            List<BloqueioHorario> conflitos = bloqueioHorarioRepository.findByProfissionalIdAndDataBloqueioAndHoraFimAfterAndHoraInicioBeforeAndIdIsNot(
                dto.getProfissionalId(),
                dto.getDataBloqueio(),
                dto.getHoraInicio(),
                dto.getHoraFim(),
                id
            );
            if (!conflitos.isEmpty()) {
                throw new IllegalArgumentException("Já existe outro bloqueio para esta profissional neste novo período.");
            }
        }
        BeanUtils.copyProperties(dto, bloqueioExistente, "id");
        return toDTO(bloqueioHorarioRepository.save(bloqueioExistente));
    }

    @Transactional
    public void excluirBloqueioHorario(Long id) {
        if (!bloqueioHorarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Bloqueio de horário não encontrado com ID: " + id);
        }
        bloqueioHorarioRepository.deleteById(id);
    }

    public List<BloqueioHorarioDTO> buscarBloqueiosPorProfissionalEData(UUID profissionalId, LocalDate data) {
        return bloqueioHorarioRepository.findByProfissionalIdAndDataBloqueio(profissionalId, data).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ######################################################################################
    // ## MÉTODO NOVO E CRUCIAL: isPeriodoDisponivelParaAgendamento                       ##
    // ## Este método é chamado pelo AgendamentoService para verificar bloqueios          ##
    // ## ANTES de um agendamento ser salvo.                                              ##
    // ######################################################################################
    public boolean isPeriodoDisponivelParaAgendamento(UUID profissionalId, LocalDateTime inicioAgendamento, LocalDateTime fimAgendamento, Long agendamentoIdParaExcluir) {
        LocalDate dataAgendamento = inicioAgendamento.toLocalDate();
        LocalTime horaInicioAgendamento = inicioAgendamento.toLocalTime();
        LocalTime horaFimAgendamento = fimAgendamento.toLocalTime();

        List<BloqueioHorario> bloqueiosNoDia;
        // Na prática, agendamentoIdParaExcluir não é usado aqui para BLOQUEIOS,
        // pois a lógica de "excluir" é para o próprio agendamento sendo atualizado.
        // Bloqueios são verificados de forma absoluta.
        bloqueiosNoDia = bloqueioHorarioRepository.findByProfissionalIdAndDataBloqueio(profissionalId, dataAgendamento);


        boolean conflitoComBloqueio = bloqueiosNoDia.stream()
            .anyMatch(b -> {
                LocalDateTime bloqueioInicioReal;
                LocalDateTime bloqueioFimReal;

                // Lidar com bloqueios de dia inteiro (horaInicio e horaFim nulos)
                if (b.getHoraInicio() == null || b.getHoraFim() == null) {
                    bloqueioInicioReal = dataAgendamento.atStartOfDay(); // Início do dia
                    bloqueioFimReal = dataAgendamento.atTime(23, 59, 59); // Fim do dia
                } else {
                    bloqueioInicioReal = dataAgendamento.atTime(b.getHoraInicio());
                    bloqueioFimReal = dataAgendamento.atTime(b.getHoraFim());
                }

                // Critério de sobreposição: (Início do NOVO antes do Fim do EXISTENTE) E (Fim do NOVO depois do Início do EXISTENTE)
                return (inicioAgendamento.isBefore(bloqueioFimReal) && fimAgendamento.isAfter(bloqueioInicioReal));
            });

        return !conflitoComBloqueio; // Retorna true se NÃO houver conflito, false se houver
    }

    // --- Métodos de Conversão (Entity para DTO) ---
    private BloqueioHorarioDTO toDTO(BloqueioHorario bloqueio) {
        BloqueioHorarioDTO dto = new BloqueioHorarioDTO();
        BeanUtils.copyProperties(bloqueio, dto);
        return dto;
    }
}