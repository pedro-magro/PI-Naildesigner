package br.com.naildesigner.agendamento_service_AgendeNail.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.antlr.v4.runtime.misc.NotNull;

import br.com.naildesigner.agendamento_service_AgendeNail.enums.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "agendamentos") // Nome da tabela no DB
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private UUID clienteId; // ID do cliente (vinculado ao Usuario-service)

    @NotNull
    private Long servicoId; // ID do serviço (vinculado ao Servico-service)

    @NotNull 
    private UUID profissionalId;

    @NotNull
    private LocalDateTime dataHoraInicio;

    @NotNull
    private LocalDateTime dataHoraFim; // Calculado com base na duração do serviço

    @Enumerated(EnumType.STRING) // Armazena o nome do enum como String no DB
    private AgendamentoStatus status;

    @Column(length = 500) // Observações podem ser mais longas
    private String observacoes;

    // Construtor padrão necessário para JPA
    public Agendamento() {}

    // Construtor completo (útil para testes e criação de objetos)
    public Agendamento(UUID clienteId, Long servicoId, UUID profissionalId, LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, AgendamentoStatus status, String observacoes) {
        this.clienteId = clienteId;
        this.servicoId = servicoId;
        this.profissionalId=profissionalId;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.status = status;
        this.observacoes = observacoes;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public void setClienteId(UUID clienteId) {
        this.clienteId = clienteId;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public AgendamentoStatus getStatus() {
        return status;
    }

    public void setStatus(AgendamentoStatus status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public UUID getProfissionalId() {
        return profissionalId;
    }

    public void setProfissionalId(UUID profissionalId) {
        this.profissionalId = profissionalId;
    }
}
