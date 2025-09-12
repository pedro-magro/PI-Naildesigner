package br.com.naildesigner.agendamento_service_AgendeNail.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

// Supondo que ServicoDTOForAgendamento está no mesmo pacote ou importado corretamente
// Ex: import br.com.naildesigner.agendamento_service_AgendeNail.dtos.ServicoDTOForAgendamento;
// Ou: import br.com.naildesigner.servico_service_api.dtos.ServicoDTOForAgendamento;

@FeignClient(name = "servico-service") // Usa o nome do serviço no Eureka
public interface ServicoClient {

    /**
     * Busca um serviço por ID.
     *
     * O path aqui deve corresponder ao endpoint exposto pelo ServicoController no servico-service.
     * 
     * - Se o ServicoController estiver em @RequestMapping("/api/servicos"), este path está correto.
     * - Se estiver em @RequestMapping("/servicos"), então o path aqui deveria ser "/servicos/{id}".
     *   O API Gateway, neste caso, deveria ter StripPrefix=1 para a rota /api/servicos/**.
     *
     * Dado o erro anterior, é provável que o servico-service espere /api/servicos/{id}.
     */
    @GetMapping("/api/servicos/{id}")
    ResponseEntity<ServicoDTOForAgendamento> getServicoById(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String bearerToken // Para propagar o JWT
    );
}
