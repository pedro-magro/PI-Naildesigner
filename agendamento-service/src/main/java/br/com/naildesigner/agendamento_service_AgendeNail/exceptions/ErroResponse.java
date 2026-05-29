package br.com.naildesigner.agendamento_service_AgendeNail.exceptions;

import java.time.LocalDateTime;

public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String message
) {
}
