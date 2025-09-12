package br.com.nailDesigner.messaging.api.dto;

import java.util.List;

public record EmailDto(List<String> to, String subject, String body) {

}

