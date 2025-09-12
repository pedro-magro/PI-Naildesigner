package br.com.naildesigner.agendamento_service_AgendeNail.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

// Se estiver usando Lombok, adicione:
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

//@Data // Se estiver usando Lombok
//@NoArgsConstructor // Se estiver usando Lombok
//@AllArgsConstructor // Se estiver usando Lombok
public class BloqueioHorarioDTO {
    private Long id;
    private LocalDate dataBloqueio;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private UUID profissionalId; // NOVO CAMPO NO DTO
    private String motivo;

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