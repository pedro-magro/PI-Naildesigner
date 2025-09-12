package br.com.naildesigner.agendamento_service_AgendeNail.clients;

import java.util.List;

public class EmailRequestDto {

    private List<String> to; // MODIFICADO para List<String>
    private String subject;
    private String body;

    public EmailRequestDto() {
    }

    public EmailRequestDto(List<String> to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    // Getters
    public List<String> getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    // Setters
    public void setTo(List<String> to) {
        this.to = to;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
