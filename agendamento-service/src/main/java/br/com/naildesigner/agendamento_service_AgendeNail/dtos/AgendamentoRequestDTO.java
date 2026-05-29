package br.com.naildesigner.agendamento_service_AgendeNail.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoRequestDTO(
    @NotNull Long servicoId,
    @NotNull UUID profissionalId,
    @NotNull LocalDateTime dataHoraInicio,
    String observacoes
) {}
