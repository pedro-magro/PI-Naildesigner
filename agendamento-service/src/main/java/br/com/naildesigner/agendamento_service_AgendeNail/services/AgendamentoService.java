package br.com.naildesigner.agendamento_service_AgendeNail.services;

import br.com.naildesigner.agendamento_service_AgendeNail.clients.MessagingServiceClient;
import br.com.naildesigner.agendamento_service_AgendeNail.clients.EmailRequestDto;
import br.com.naildesigner.agendamento_service_AgendeNail.clients.ServicoClient;
import br.com.naildesigner.agendamento_service_AgendeNail.dtos.*;
import br.com.naildesigner.agendamento_service_AgendeNail.enums.AgendamentoStatus;
import br.com.naildesigner.agendamento_service_AgendeNail.models.Agendamento;
import br.com.naildesigner.agendamento_service_AgendeNail.repositories.AgendamentoRepository;
import br.com.naildesigner.agendamento_service_AgendeNail.clients.ServicoDTOForAgendamento;
import br.com.naildesigner.agendamento_service_AgendeNail.clients.AuthServiceClient;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class AgendamentoService {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoService.class);

    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private BloqueioHorarioService bloqueioHorarioService;
    @Autowired private ServicoClient servicoClient;
    @Autowired private MessagingServiceClient messagingServiceClient;
    @Autowired private JwtService jwtService;
    @Autowired private AuthServiceClient authServiceClient;

    private static final LocalTime HORA_INICIO_TRABALHO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIM_TRABALHO = LocalTime.of(18, 0);
    private static final int INTERVALO_SLOT_MINUTOS = 30;

    
    @Transactional
    public AgendamentoDTO salvarAgendamento(AgendamentoRequestDTO dto, Authentication authentication) {
        // 1. Extração de dados do utilizador e do serviço
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID clienteId = UUID.fromString(jwt.getSubject());
        String token = jwt.getTokenValue();
        
        ServicoDTOForAgendamento servico = buscarServico(dto.servicoId(), token);
        LocalDateTime dataHoraFim = calcularHorarioFim(dto.dataHoraInicio(),servico.getDuracao());

        // 2. Validações e criação do agendamento
        validarConflitos(dto.profissionalId(), dto.dataHoraInicio(), dataHoraFim, null);

        Agendamento agendamento = new Agendamento();
        agendamento.setClienteId(clienteId);
        agendamento.setProfissionalId(dto.profissionalId());
        agendamento.setServicoId(dto.servicoId());
        agendamento.setDataHoraInicio(dto.dataHoraInicio());
        agendamento.setDataHoraFim(dataHoraFim);
        agendamento.setStatus(AgendamentoStatus.CONFIRMADO);
        agendamento.setObservacoes(dto.observacoes());

        Agendamento salvo = agendamentoRepository.save(agendamento);
        logger.info("Agendamento ID {} criado para o cliente {}", salvo.getId(), clienteId);
        
        // 3. Lógica de envio de email
        try {
            // Busca os emails do cliente e do profissional
            String emailCliente = authServiceClient.getEmailPorId(clienteId); 
            String emailProfissional = authServiceClient.getEmailPorId(dto.profissionalId());

            List<String> destinatarios = new ArrayList<>();
            if (emailCliente != null && !emailCliente.isBlank()) destinatarios.add(emailCliente);
            if (emailProfissional != null && !emailProfissional.isBlank()) destinatarios.add(emailProfissional);

            if (!destinatarios.isEmpty()) {
                String assunto = "Confirmação de Agendamento - Agende Nail";
                String corpo = String.format(
                    "Olá! O seu agendamento para o serviço '%s' foi confirmado para o dia %s às %s.",
                    servico.getNome(),
                    salvo.getDataHoraInicio().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    salvo.getDataHoraInicio().toLocalTime()
                );
                
                EmailRequestDto emailRequest = new EmailRequestDto(destinatarios, assunto, corpo);
                
                // Constrói o cabeçalho de autorização completo
                String bearerToken = "Bearer " + token;

                // CHAMA O MÉTODO CORRETO ("sendEmail") com os parâmetros corretos
                messagingServiceClient.sendEmail(emailRequest, bearerToken);

                logger.info("Pedido de envio de email para o agendamento ID {}.", salvo.getId());
            } else {
                 logger.warn("Nenhum email de destinatário válido encontrado para o agendamento ID {}.", salvo.getId());
            }
        } catch (Exception e) {
            logger.error("Falha ao tentar enviar email para o agendamento ID {}: {}", salvo.getId(), e.getMessage());
        }
        
        return new AgendamentoDTO(salvo);
    }

    public List<AgendamentoDTO> listarAgendamentosPorCliente(UUID clienteId) {
        return agendamentoRepository.findByClienteId(clienteId)
                .stream()
                .map(AgendamentoDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Cancela um agendamento, validando se o usuário autenticado é o dono.
     * VERSÃO FINAL E CORRETA
     * * @param id O ID do agendamento a ser cancelado.
     * @param authentication O objeto de autenticação do usuário logado.
     * @return O DTO do agendamento atualizado com o status "CANCELADO".
     * @throws EntityNotFoundException se o agendamento não for encontrado.
     * @throws IllegalAccessException se o usuário tentar cancelar um agendamento que não é seu.
     * @throws IllegalArgumentException se o agendamento já estiver concluído ou cancelado.
     */
    @Transactional
    public AgendamentoDTO cancelarAgendamento(Long id, Authentication authentication) throws IllegalAccessException {
        
        // 1. Extrai o ID do usuário (UUID) do token JWT.
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new IllegalAccessException("Acesso negado: informações de autenticação inválidas.");
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID clienteIdAutenticado = UUID.fromString(jwt.getSubject());

        // 2. Busca o agendamento no banco de dados.
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + id));

        // 3. Validação de Segurança: Verifica se o usuário autenticado é o dono do agendamento.
        if (!agendamento.getClienteId().equals(clienteIdAutenticado)) {
            throw new IllegalAccessException("Você não tem permissão para cancelar este agendamento.");
        }

        // 4. Validação de Regra de Negócio: Não permite cancelar agendamentos já finalizados.
        if (agendamento.getStatus() == AgendamentoStatus.CONCLUIDO || agendamento.getStatus() == AgendamentoStatus.CANCELADO) {
            throw new IllegalArgumentException("Este agendamento não pode mais ser cancelado pois já foi " + agendamento.getStatus().toString().toLowerCase());
        }

        // 5. Altera o status e salva no banco.
        agendamento.setStatus(AgendamentoStatus.CANCELADO);
        Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

        // 6. Retorna o DTO com os dados atualizados.
        return new AgendamentoDTO(agendamentoSalvo); // Assumindo que você tem um construtor que aceita a entidade.
    }
    
    public List<AgendamentoDTO> listarTodosAgendamentos() {
        return agendamentoRepository.findAll().stream()
                .map(AgendamentoDTO::new)
                .collect(Collectors.toList());
    }

    public AgendamentoDTO buscarAgendamentoPorId(Long id) {
        return agendamentoRepository.findById(id)
                .map(AgendamentoDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com ID: " + id));
    }
    
    @Transactional
    public AgendamentoDTO atualizarAgendamento(Long id, AgendamentoDTO dto, Authentication authentication) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com ID: " + id));

        String token = getBearerTokenFromRequest();
        ServicoDTOForAgendamento servico = buscarServico(dto.getServicoId(), token);
        LocalDateTime dataHoraFim = calcularHorarioFim(dto.getDataHoraInicio(), servico.getDuracao());

        validarConflitos(dto.getProfissionalId(), dto.getDataHoraInicio(), dataHoraFim, id);

        agendamento.setClienteId(dto.getClienteId());
        agendamento.setProfissionalId(dto.getProfissionalId());
        agendamento.setServicoId(dto.getServicoId());
        agendamento.setDataHoraInicio(dto.getDataHoraInicio());
        agendamento.setDataHoraFim(dataHoraFim);
        agendamento.setStatus(dto.getStatus());
        agendamento.setObservacoes(dto.getObservacoes());

        return new AgendamentoDTO(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public void excluirAgendamento(Long id) {
        if (!agendamentoRepository.existsById(id)) {
            throw new EntityNotFoundException("Agendamento não encontrado com ID: " + id);
        }
        agendamentoRepository.deleteById(id);
    }

    public DisponibilidadeDTO buscarHorariosDisponiveis(LocalDate data, Long servicoId, UUID profissionalId, Authentication authentication) {
        String token = getBearerTokenFromRequest();
        ServicoDTOForAgendamento servico = buscarServico(servicoId, token);
        int duracaoServicoMinutos = servico.getDuracao();

        List<LocalTime> todosOsSlotsPotenciais = gerarSlotsPotenciais(duracaoServicoMinutos);
        List<Agendamento> agendamentosNoDia = agendamentoRepository.findByProfissionalIdAndDataHoraInicioBetweenAndStatusNot(
            profissionalId, data.atStartOfDay(), data.atTime(23, 59, 59), AgendamentoStatus.CANCELADO);
        
        List<String> horariosDisponiveis = todosOsSlotsPotenciais.stream()
            .filter(slot -> {
                LocalDateTime tentativaInicio = data.atTime(slot);
                LocalDateTime tentativaFim = calcularHorarioFim(tentativaInicio, duracaoServicoMinutos);
                boolean conflitoAgendamento = agendamentosNoDia.stream()
                    .anyMatch(a -> tentativaInicio.isBefore(a.getDataHoraFim()) && tentativaFim.isAfter(a.getDataHoraInicio()));
                boolean conflitoBloqueio = !bloqueioHorarioService.isPeriodoDisponivelParaAgendamento(profissionalId, tentativaInicio, tentativaFim, null);
                return !conflitoAgendamento && !conflitoBloqueio;
            })
            .map(slot -> slot.format(DateTimeFormatter.ofPattern("HH:mm")))
            .collect(Collectors.toList());

        return new DisponibilidadeDTO(data, horariosDisponiveis);
    }

    private String getBearerTokenFromRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra != null) {
            HttpServletRequest request = sra.getRequest();
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                return header.substring(7);
            }
        }
        throw new IllegalStateException("Token de autorização não encontrado na requisição.");
    }
    
    private ServicoDTOForAgendamento buscarServico(Long servicoId, String bearerToken) {
        try {
            // A chamada agora está correta, passando os dois argumentos que o ServicoClient espera.
            ResponseEntity<ServicoDTOForAgendamento> respostaServico = servicoClient.getServicoById(servicoId, bearerToken);
            if (respostaServico.getStatusCode().is2xxSuccessful() && respostaServico.getBody() != null) {
                return respostaServico.getBody();
            }
        } catch (Exception e) {
            logger.error("Falha na comunicação com servico-service para ID {}: {}", servicoId, e.getMessage());
        }
        throw new EntityNotFoundException("Serviço não encontrado ou inacessível com ID: " + servicoId);
    }

    private void validarConflitos(UUID profissionalId, LocalDateTime inicio, LocalDateTime fim, Long agendamentoIdExcluido) {
       if(!validarDisponibilidade(profissionalId, inicio, fim, agendamentoIdExcluido)){
           throw new IllegalArgumentException("Horário inválido, conflita com outro atendimento.");
       }
    }
    public boolean validarDisponibilidade(UUID profissional, LocalDateTime inicio, LocalDateTime fim, Long agendamentoIdExcluido){
        List<Agendamento> conflitos = (agendamentoIdExcluido == null)
                ? agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndStatusNot(profissional, inicio, fim, AgendamentoStatus.CANCELADO)
                : agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndIdNotAndStatusNot(profissional, inicio, fim, agendamentoIdExcluido, AgendamentoStatus.CANCELADO);
        return conflitos.isEmpty();
        
                
    }
    
    private List<LocalTime> gerarSlotsPotenciais(int duracaoServicoMinutos) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime slot = HORA_INICIO_TRABALHO;
        while (!slot.plusMinutes(duracaoServicoMinutos).isAfter(HORA_FIM_TRABALHO)) {
            slots.add(slot);
            slot = slot.plusMinutes(INTERVALO_SLOT_MINUTOS);
        }
        return slots;
    }
    
    public List<AgendamentoDTO> listarAgendamentosPorCliente(Authentication authentication) {
        // 1. Extrai o ID do cliente de forma segura a partir do token
        String token = getBearerTokenFromRequest(); // Usa o método auxiliar que já temos
        UUID clienteId = jwtService.extractUserId(token);

        // 2. Busca os agendamentos no repositório
        List<Agendamento> agendamentos = agendamentoRepository.findByClienteId(clienteId);

        // 3. Converte as entidades para DTOs e retorna
        return agendamentos.stream()
                .map(AgendamentoDTO::new) // Usa o construtor do DTO que criámos
                .collect(Collectors.toList());
    }
    
    @Transactional
    public AgendamentoDTO salvarAgendamentoPeloAdmin(AgendamentoDTO dto, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String bearerToken = "Bearer " + jwt.getTokenValue();

        ServicoDTOForAgendamento servico = buscarServico(dto.getServicoId(), bearerToken);
        LocalDateTime dataHoraFim = calcularHorarioFim(dto.getDataHoraInicio(),servico.getDuracao());

        // --- VALIDAÇÃO DE CONFLITOS ANTES DE SALVAR ---
        validarConflitos(dto.getProfissionalId(), dto.getDataHoraInicio(), dataHoraFim, null);
        
        Agendamento agendamento = new Agendamento();
        // ... (preenchimento dos campos do agendamento)
        agendamento.setClienteId(dto.getClienteId());
        agendamento.setProfissionalId(dto.getProfissionalId());
        agendamento.setServicoId(dto.getServicoId());
        agendamento.setDataHoraInicio(dto.getDataHoraInicio());
        agendamento.setDataHoraFim(dataHoraFim);
        agendamento.setStatus(dto.getStatus());
        agendamento.setObservacoes(dto.getObservacoes());

        Agendamento salvo = agendamentoRepository.save(agendamento);
        logger.info("Admin criou/salvou o agendamento ID {}", salvo.getId());
        
        enviarEmailDeConfirmacao(salvo, servico, authentication);
        return new AgendamentoDTO(salvo);
    }
    
    @Transactional
    public AgendamentoDTO atualizarAgendamentoPeloAdmin(Long id, AgendamentoDTO dto, Authentication authentication) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com ID: " + id));
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String bearerToken = "Bearer " + jwt.getTokenValue();

        // A chamada agora passa o token, que é o segundo argumento esperado.
        ServicoDTOForAgendamento servico = buscarServico(dto.getServicoId(), bearerToken);
        LocalDateTime dataHoraFim = calcularHorarioFim(dto.getDataHoraInicio(), servico.getDuracao());
        
        validarConflitos(dto.getProfissionalId(), dto.getDataHoraInicio(), dataHoraFim, id);

        agendamento.setClienteId(dto.getClienteId());
        agendamento.setProfissionalId(dto.getProfissionalId());
        agendamento.setServicoId(dto.getServicoId());
        agendamento.setDataHoraInicio(dto.getDataHoraInicio());
        agendamento.setDataHoraFim(dataHoraFim);
        agendamento.setStatus(dto.getStatus());
        agendamento.setObservacoes(dto.getObservacoes());

        Agendamento salvo = agendamentoRepository.save(agendamento);
        logger.info("Admin atualizou o agendamento ID {}", salvo.getId());

        return new AgendamentoDTO(salvo);
    }
    public LocalDateTime calcularHorarioFim(LocalDateTime inicio, int duracaoMinutos){
        return inicio.plusMinutes(duracaoMinutos);
    }
    private void enviarEmailDeConfirmacao(Agendamento agendamento, ServicoDTOForAgendamento servico, Authentication authentication) {
        try {
            // Extrai o token para a chamada ao messaging-service.
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String bearerToken = "Bearer " + jwt.getTokenValue();

            // Busca os emails do cliente e do profissional no auth-service.
            String emailCliente = authServiceClient.getEmailPorId(agendamento.getClienteId()); 
            String emailProfissional = authServiceClient.getEmailPorId(agendamento.getProfissionalId());

            List<String> destinatarios = new ArrayList<>();
            if (emailCliente != null && !emailCliente.isBlank()) destinatarios.add(emailCliente);
            if (emailProfissional != null && !emailProfissional.isBlank()) destinatarios.add(emailProfissional);

            if (!destinatarios.isEmpty()) {
                String assunto = "Confirmação de Agendamento - Agende Nail";
                String corpo = String.format(
                    "Olá!\n\nUm agendamento para o serviço '%s' foi confirmado.\n\nData: %s\nHora: %s\n\nAté breve!",
                    servico.getNome(),
                    agendamento.getDataHoraInicio().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    agendamento.getDataHoraInicio().toLocalTime()
                );
                
                EmailRequestDto emailRequest = new EmailRequestDto(destinatarios, assunto, corpo);
                
                // Chama o messaging-service para enviar o email.
                messagingServiceClient.sendEmail(emailRequest, bearerToken);
                logger.info("Pedido de envio de email para o agendamento ID {}.", agendamento.getId());
            }
        } catch (Exception e) {
            // Uma falha aqui não deve quebrar a operação principal de agendamento.
            logger.error("Falha ao tentar enviar email para o agendamento ID {}: {}", agendamento.getId(), e.getMessage());
        }
    }

}
