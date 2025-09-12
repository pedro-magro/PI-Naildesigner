package br.com.naildesigner.agendamento_service_AgendeNail.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.antlr.v4.runtime.misc.NotNull;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Se estiver usando Lombok, adicione:
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

//@Data // Se estiver usando Lombok
//@NoArgsConstructor // Se estiver usando Lombok
//@AllArgsConstructor // Se estiver usando Lombok
@Entity
public class BloqueioHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate dataBloqueio;

    private LocalTime horaInicio; // Pode ser null para bloqueio de dia inteiro
    private LocalTime horaFim;    // Pode ser null para bloqueio de dia inteiro

    // NOVO CAMPO: ID da Profissional cujo horário será bloqueado
    @NotNull
    private UUID profissionalId;

    private String motivo; // Ex: Férias, Consulta, Feriado

    // Construtores (se não usar Lombok, adicione manualmente)
    // Getters e Setters (se não usar Lombok, adicione manualmente)
    // ... inclua os getters e setters para profissionalId ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDataBloqueio() { return dataBloqueio; }
    public void setDataBloqueio(LocalDate dataBloqueio) { this.dataBloqueio = dataBloqueio; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public UUID getProfissionalId() { return profissionalId; } // NOVO GETTER
    public void setProfissionalId(UUID profissionalId) { this.profissionalId = profissionalId; } // NOVO SETTER
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}