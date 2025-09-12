package br.com.naildesigner.agendamento_service_AgendeNail.dtos;

import br.com.naildesigner.agendamento_service_AgendeNail.enums.AgendamentoStatus;
import br.com.naildesigner.agendamento_service_AgendeNail.models.Agendamento;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) para representar um Agendamento.
 * É usado para transferir dados entre o backend e o frontend de forma segura.
 */
public class AgendamentoDTO {

    private Long id;
    private UUID clienteId;
    private String clienteNome;
    private Long servicoId;
    private String servicoNome;
    private Double servicoPreco;
    private Integer servicoDuracao;
    private UUID profissionalId;
    private String data;
    private String hora;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private AgendamentoStatus status;
    private String observacoes;

    // Construtor padrão - necessário para o Jackson (conversor JSON)
    public AgendamentoDTO() {}

    // Construtor que converte uma Entidade 'Agendamento' em um 'AgendamentoDTO'.
    // Esta é a forma recomendada de fazer a conversão no seu service.
    public AgendamentoDTO(Agendamento agendamento) {
        this.id = agendamento.getId();
        this.clienteId = agendamento.getClienteId();
        this.profissionalId = agendamento.getProfissionalId();
        this.servicoId = agendamento.getServicoId();
        this.dataHoraInicio = agendamento.getDataHoraInicio();
        this.dataHoraFim = agendamento.getDataHoraFim();
        this.status = agendamento.getStatus();
        this.observacoes = agendamento.getObservacoes();

        // Formata a data e a hora para serem exibidas facilmente no front-end
        if (agendamento.getDataHoraInicio() != null) {
            this.data = agendamento.getDataHoraInicio().format(DateTimeFormatter.ISO_LOCAL_DATE); // ex: "2025-06-09"
            this.hora = agendamento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")); // ex: "14:30"
        }
    }

    // --- Getters e Setters para todos os campos ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }
    public String getServicoNome() { return servicoNome; }
    public void setServicoNome(String servicoNome) { this.servicoNome = servicoNome; }
    public Double getServicoPreco() { return servicoPreco; }
    public void setServicoPreco(Double servicoPreco) { this.servicoPreco = servicoPreco; }
    public Integer getServicoDuracao() { return servicoDuracao; }
    public void setServicoDuracao(Integer servicoDuracao) { this.servicoDuracao = servicoDuracao; }
    public UUID getProfissionalId() { return profissionalId; }
    public void setProfissionalId(UUID profissionalId) { this.profissionalId = profissionalId; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(LocalDateTime dataHoraInicio) { this.dataHoraInicio = dataHoraInicio; }
    public LocalDateTime getDataHoraFim() { return dataHoraFim; }
    public void setDataHoraFim(LocalDateTime dataHoraFim) { this.dataHoraFim = dataHoraFim; }
    public AgendamentoStatus getStatus() { return status; }
    public void setStatus(AgendamentoStatus status) { this.status = status; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
