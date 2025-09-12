package br.com.naildesigner.agendamento_service_AgendeNail.controllers;

import br.com.naildesigner.agendamento_service_AgendeNail.dtos.AgendamentoDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.dtos.AgendamentoRequestDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.dtos.DisponibilidadeDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.services.AgendamentoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agendamentos")

public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    // --- Endpoints para Clientes (Autenticados) ---

    /**
     * Cria um novo agendamento. O ID do cliente é obtido automaticamente
     * a partir do token JWT do usuário autenticado.
     */
    @PostMapping
    public ResponseEntity<AgendamentoDTO> criarAgendamento(
            @Valid @RequestBody AgendamentoRequestDTO dto,
            Authentication authentication) {
        try {
            AgendamentoDTO novoAgendamento = agendamentoService.salvarAgendamento(dto, authentication);
            return new ResponseEntity<>(novoAgendamento, HttpStatus.CREATED);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            // Retorna um erro 400 com a mensagem de negócio (ex: "Horário conflitante")
            return ResponseEntity.badRequest().body(null); 
        }
    }

    /**
     * Busca os horários disponíveis para uma profissional e serviço numa data específica.
     */
    @GetMapping("/disponibilidade")
    public ResponseEntity<DisponibilidadeDTO> buscarDisponibilidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam Long servicoId,
            @RequestParam UUID profissionalId,
            Authentication authentication) {
        
        DisponibilidadeDTO disponibilidade = agendamentoService.buscarHorariosDisponiveis(data, servicoId, profissionalId, authentication);
        return ResponseEntity.ok(disponibilidade);
    }
    
    /**
     * Retorna os agendamentos pertencentes ao cliente autenticado.
     */
    @GetMapping("/meus")
    public ResponseEntity<List<AgendamentoDTO>> listarMeusAgendamentos(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Pega o objeto JWT do principal da autenticação
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        // Pega o "subject" (sub) do token, que DEVE ser o ID do usuário
        String userIdString = jwt.getSubject();
        
        // Converte a String do ID para o tipo UUID
        UUID clienteId = UUID.fromString(userIdString);
        
        List<AgendamentoDTO> agendamentos = agendamentoService.listarAgendamentosPorCliente(clienteId);
        return ResponseEntity.ok(agendamentos);
    }
    /**
     * Permite que um utilizador cancele o seu próprio agendamento.
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<AgendamentoDTO> cancelarAgendamento(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // A lógica de validação de permissão deve estar dentro do seu service
            AgendamentoDTO agendamentoCancelado = agendamentoService.cancelarAgendamento(id, authentication);
            return ResponseEntity.ok(agendamentoCancelado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- Endpoints para Administradores ---

    /**
     * Lista TODOS os agendamentos no sistema. Apenas para ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgendamentoDTO>> listarTodosAgendamentos() {
        List<AgendamentoDTO> agendamentos = agendamentoService.listarTodosAgendamentos();
        return ResponseEntity.ok(agendamentos);
    }

    /**
     * Busca um agendamento específico por ID. Apenas para ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgendamentoDTO> buscarAgendamentoPorId(@PathVariable Long id) {
        try {
            AgendamentoDTO agendamento = agendamentoService.buscarAgendamentoPorId(id);
            return ResponseEntity.ok(agendamento);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AgendamentoDTO> criarAgendamentoPeloAdmin(
            @Valid @RequestBody AgendamentoDTO dto,
            Authentication authentication) { // Adicione o parâmetro Authentication
        try {
            // Passe o objeto authentication para o serviço
        	AgendamentoDTO novoAgendamento = agendamentoService.salvarAgendamentoPeloAdmin(dto, authentication);
        	return new ResponseEntity<>(novoAgendamento, HttpStatus.CREATED);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Atualiza um agendamento existente. Apenas para ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgendamentoDTO> atualizarAgendamento(@PathVariable Long id, @Valid @RequestBody AgendamentoDTO agendamentoDTO, Authentication authentication) {
        try {
            AgendamentoDTO agendamentoAtualizado = agendamentoService.atualizarAgendamento(id, agendamentoDTO, authentication);
            return ResponseEntity.ok(agendamentoAtualizado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deleta um agendamento. Apenas para ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarAgendamento(@PathVariable Long id) {
        try {
            agendamentoService.excluirAgendamento(id);
            return ResponseEntity.noContent().build(); // Retorno 204 No Content
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
