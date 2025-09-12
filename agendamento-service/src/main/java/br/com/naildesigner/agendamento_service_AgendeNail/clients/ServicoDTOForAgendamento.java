package br.com.naildesigner.agendamento_service_AgendeNail.clients;

// É uma boa prática usar Lombok para DTOs para reduzir o código boilerplate
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

//@Data // Se estiver usando Lombok
//@NoArgsConstructor // Se estiver usando Lombok
//@AllArgsConstructor // Se estiver usando Lombok
public class ServicoDTOForAgendamento {
    private Long id;
    private String nome;
    private Integer duracao; // Duração em minutos
    private Double preco;

    // Construtor padrão (necessário para desserialização JSON)
    public ServicoDTOForAgendamento() {}

    // Construtor com todos os argumentos (útil para testes ou criação manual)
    public ServicoDTOForAgendamento(Long id, String nome, Integer duracao, Double preco) {
        this.id = id;
        this.nome = nome;
        this.duracao = duracao;
        this.preco = preco;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getDuracao() {
        return duracao;
    }

    public void setDuracao(Integer duracao) {
        this.duracao = duracao;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }
}
