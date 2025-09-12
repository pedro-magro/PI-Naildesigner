package br.com.naildesigner.agendamento_service_AgendeNail.dtos;

import java.time.LocalDate;
import java.util.List;

public class DisponibilidadeDTO {
    private LocalDate data;
    private List<String> horariosDisponiveis; // Variável está correta

    // Construtor padrão
    public DisponibilidadeDTO() {}

    // Construtor completo
    public DisponibilidadeDTO(LocalDate data, List<String> horariosDisponiveis) {
        this.data = data;
        this.horariosDisponiveis = horariosDisponiveis;
    }

    // Getters e Setters CORRIGIDOS
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    // Corrigido: getHorariosDisponiveis (sem 'á' e com 'd' minúsculo)
    public List<String> getHorariosDisponiveis() { return horariosDisponiveis; }
    // Corrigido: setHorariosDisponiveis (sem 'á' e com 'd' minúsculo)
    public void setHorariosDisponiveis(List<String> horariosDisponiveis) { this.horariosDisponiveis = horariosDisponiveis; }
}
