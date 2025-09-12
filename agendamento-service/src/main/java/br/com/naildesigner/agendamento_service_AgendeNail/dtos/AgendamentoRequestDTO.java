package br.com.naildesigner.agendamento_service_AgendeNail.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

// DTO para a REQUISIÇÃO de criação de um agendamento.
// Note que não tem clienteId, pois ele será extraído do token.
public record AgendamentoRequestDTO(
    @NotNull Long servicoId,
    @NotNull UUID profissionalId,
    @NotNull LocalDateTime dataHoraInicio,
    String observacoes
) {}
