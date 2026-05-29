package br.com.naildesigner.agendamento_service_AgendeNail.services;

import br.com.nailDesigner.messaging.api.dto.EmailDto;
import br.com.naildesigner.agendamento_service_AgendeNail.clients.AuthServiceClient;
import br.com.naildesigner.agendamento_service_AgendeNail.clients.MessagingServiceClient;
import br.com.naildesigner.agendamento_service_AgendeNail.clients.ServicoClient;
import br.com.naildesigner.agendamento_service_AgendeNail.clients.ServicoDTOForAgendamento;
import br.com.naildesigner.agendamento_service_AgendeNail.dtos.AgendamentoDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.dtos.AgendamentoRequestDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.dtos.DisponibilidadeDTO;
import br.com.naildesigner.agendamento_service_AgendeNail.enums.AgendamentoStatus;
import br.com.naildesigner.agendamento_service_AgendeNail.models.Agendamento;
import br.com.naildesigner.agendamento_service_AgendeNail.repositories.AgendamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ctc.wstx.shaded.msv_core.datatype.xsd.NumberType.save;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgendamentoServiceTest {

    @Mock
    AgendamentoRepository agendamentoRepository;
    @Mock
    EmailPublisher emailPublisher;
    @Mock
    MessagingServiceClient messagingServiceClient;
    @Mock
    private BloqueioHorarioService bloqueioHorarioService;
    @Mock
    private ServicoClient servicoClient;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthServiceClient authService;

    @InjectMocks
    AgendamentoService agendamentoService;

    private LocalDateTime inicio;
    private LocalDateTime fim;
    private UUID profissionalId;
    Long agendamentoId;

    private UUID clienteId;

     private Jwt jwt;

    private Authentication authentication;

    private AgendamentoRequestDTO agendamentoDto;

    private Long id;

    private Agendamento agendamento;


    @BeforeEach
    public void setUp() {
        inicio = LocalDateTime.of(2023, 10, 1, 10, 0);
        fim = LocalDateTime.of(2023, 10, 1, 10, 30);
        profissionalId = UUID.randomUUID();
        agendamentoId = 15L;

        clienteId = UUID.randomUUID();

        jwt = Jwt.withTokenValue("token-teste")
                .header("alg", "none")
                .subject(clienteId.toString())
                .build();

        authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(jwt);

        agendamentoDto  = new AgendamentoRequestDTO(
                1L,
                UUID.randomUUID(),
                LocalDateTime.of(2026, 4, 10, 10, 0),
                "teste"
        );

        id = 1L;
        agendamento = new Agendamento();
        agendamento.setId(id);
        agendamento.setClienteId(clienteId);
        agendamento.setProfissionalId(profissionalId);
        agendamento.setServicoId(10L);
        agendamento.setDataHoraInicio(LocalDateTime.of(2026, 4, 10, 13, 0));
        agendamento.setDataHoraFim(LocalDateTime.of(2026, 4, 10, 12,0));
        agendamento.setObservacoes("Teste");
        agendamento.setStatus(AgendamentoStatus.CONFIRMADO);

    }

    LocalDateTime dataHoraInicio = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime esperado = LocalDateTime.of(2023, 10, 1, 10, 30);

    @Test
    public void deveCalcularHorarioDeFimCorretamente(){
       LocalDateTime fimAgendamento = agendamentoService.calcularHorarioFim(dataHoraInicio, 30);

       assertEquals(esperado, fimAgendamento);
    }

    @Test
    public void deveValidarDisponibilidadeCorretamente(){
        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndIdNotAndStatusNot(profissionalId, inicio, fim, agendamentoId, AgendamentoStatus.CANCELADO)).thenReturn(new ArrayList<>());
        boolean disponivel = agendamentoService.validarDisponibilidade(profissionalId, inicio, fim, agendamentoId);
        assertTrue(disponivel);
    }

    @Test
    public void deveDarFalseQuandoNaoTiverDisponibilidade(){
        Agendamento agendamento = new Agendamento();
        agendamento.setProfissionalId(profissionalId);
        agendamento.setDataHoraInicio(inicio);
        agendamento.setDataHoraFim(fim);
        agendamento.setId(1L);
        agendamento.setStatus(AgendamentoStatus.PENDENTE);

        List<Agendamento> conflitos = new ArrayList<>();
        conflitos.add(agendamento);

        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndIdNotAndStatusNot(profissionalId, inicio, fim, agendamentoId, AgendamentoStatus.CANCELADO)).thenReturn(conflitos);

        assertFalse(agendamentoService.validarDisponibilidade(profissionalId, inicio, fim, agendamentoId));

    }

    @Test
    void deveUsarFeignComoFallbackQuandoRabbitFalhar() {
        EmailDto emailDto = new EmailDto(List.of("cliente@nail.com"), "Assunto", "Corpo");
        ReflectionTestUtils.setField(agendamentoService, "emailFallbackEnabled", true);
        doThrow(new RuntimeException("rabbit indisponivel")).when(emailPublisher).publishEmail(emailDto);

        ReflectionTestUtils.invokeMethod(agendamentoService, "publicarEmailComFallback", emailDto, "token-123");

        verify(messagingServiceClient).sendEmail(eq(emailDto), eq("Bearer token-123"));
    }

    @Test
    void devePropagarErroQuandoFallbackFeignEstiverDesabilitado() {
        EmailDto emailDto = new EmailDto(List.of("cliente@nail.com"), "Assunto", "Corpo");
        ReflectionTestUtils.setField(agendamentoService, "emailFallbackEnabled", false);
        doThrow(new RuntimeException("rabbit indisponivel")).when(emailPublisher).publishEmail(emailDto);

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> ReflectionTestUtils.invokeMethod(agendamentoService, "publicarEmailComFallback", emailDto, "token-123")
        );

        assertEquals("rabbit indisponivel", exception.getMessage());
    }

    @Test
    void deveSalvarAgendamentoCorretamente() {



        ServicoDTOForAgendamento servico = new ServicoDTOForAgendamento();
        servico.setNome("teste");
        servico.setDuracao(90);

        when(servicoClient.getServicoById(eq(1L), anyString()))
                .thenReturn(ResponseEntity.ok(servico));

        when(agendamentoRepository
                .findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndStatusNot(
                        any(), any(), any(), eq(AgendamentoStatus.CANCELADO)))
                .thenReturn(List.of());

        when(authService.getEmailPorId(any()))
                .thenReturn("teste@email.com");

        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(invocation -> {
                    Agendamento agendamento = invocation.getArgument(0);
                    agendamento.setId(10L);
                    return agendamento;
                });

        AgendamentoDTO resultado = agendamentoService.salvarAgendamento(agendamentoDto, authentication);
        ArgumentCaptor<Agendamento> captor = ArgumentCaptor.forClass(Agendamento.class);
        verify(agendamentoRepository).save(captor.capture());

        Agendamento salvo = captor.getValue();

        assertEquals(clienteId, salvo.getClienteId());
        assertEquals(agendamentoDto.profissionalId(), salvo.getProfissionalId());
        assertEquals(agendamentoDto.servicoId(), salvo.getServicoId());
        assertEquals(agendamentoDto.dataHoraInicio(), salvo.getDataHoraInicio());
        assertEquals(agendamentoDto.dataHoraInicio().plusMinutes(90), salvo.getDataHoraFim());
        assertEquals(AgendamentoStatus.CONFIRMADO, salvo.getStatus());
        assertEquals("teste", salvo.getObservacoes());

        verify(emailPublisher).publishEmail(any(EmailDto.class));

        assertEquals(10L, resultado.getId());
        assertEquals(clienteId, resultado.getClienteId());

    }

    @Test
    public void deveLancarExceptionQuandoAgendamentoSobreposto() {
        ServicoDTOForAgendamento servico = new ServicoDTOForAgendamento();
        servico.setDuracao(60);

        when(servicoClient.getServicoById(eq(1L), anyString()))
            .thenReturn(ResponseEntity.ok(servico));

        when(agendamentoRepository
                .findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndStatusNot(
                        any(), any(), any(), eq(AgendamentoStatus.CANCELADO)))
                .thenReturn(List.of(new Agendamento()));

        assertThrows(IllegalArgumentException.class, () ->
                agendamentoService.salvarAgendamento(agendamentoDto, authentication));
    }

    @Test
    public void deveFalharAoNaoReceberServicoValido(){
        when(servicoClient.getServicoById(eq(1L), anyString()))
                .thenThrow(new RuntimeException("erro"));

        assertThrows(EntityNotFoundException.class, () ->
                agendamentoService.salvarAgendamento(agendamentoDto, authentication));
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    public void deveSalvarAgendamentoMesmoQuandoEnvioDeEmailFalha(){
        ServicoDTOForAgendamento servico = new ServicoDTOForAgendamento();
        servico.setDuracao(60);
        servico.setNome("Manicure");

        when(servicoClient.getServicoById(eq(1L), anyString()))
                .thenReturn(ResponseEntity.ok(servico));

        when(agendamentoRepository
                .findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndStatusNot(
                        any(), any(), any(), eq(AgendamentoStatus.CANCELADO)))
                .thenReturn(List.of());
        when(authService.getEmailPorId(any()))
                .thenReturn("teste@email.com");

        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(invocation -> {
                    Agendamento agendamento = invocation.getArgument(0);
                    agendamento.setId(10L);
                    return agendamento;
                });
        doThrow(new RuntimeException("falha ao enviar email de confirmação"))
                .when(emailPublisher).publishEmail(any(EmailDto.class));

        AgendamentoDTO resultado = agendamentoService.salvarAgendamento(agendamentoDto, authentication);

        verify(agendamentoRepository).save(any());
        assertEquals(10L, resultado.getId());
    }

    @Test
    public void naoDeveEnviarEmailQuandoNaoHouveremEmailsValidos(){
        ServicoDTOForAgendamento servico = new ServicoDTOForAgendamento();
        servico.setDuracao(60);
        servico.setNome("Manicure");

        when(servicoClient.getServicoById(eq(1L), anyString())).thenReturn(ResponseEntity.ok(servico));
        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(authService.getEmailPorId(any(UUID.class))).thenReturn("");

        agendamentoService.salvarAgendamento(agendamentoDto, authentication);

        verify(emailPublisher, never()).publishEmail(any(EmailDto.class));
    }

    @Test
    public void deveCancelarAgendamentoComSucesso() throws IllegalAccessException{

        when(agendamentoRepository.findById(id)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoDTO resultado = agendamentoService.cancelarAgendamento(id, authentication);

        ArgumentCaptor<Agendamento> captor = ArgumentCaptor.forClass(Agendamento.class);
        verify(agendamentoRepository).save(captor.capture());

        Agendamento salvo = captor.getValue();
        assertEquals(AgendamentoStatus.CANCELADO, salvo.getStatus());

        assertEquals(id, resultado.getId());
        assertEquals(AgendamentoStatus.CANCELADO, resultado.getStatus());
    }

    @Test
    public void deveLancarExcecaoQuandoAuthenticationForNull(){
        assertThrows(IllegalAccessException.class, () ->
                agendamentoService.cancelarAgendamento(1L, null));

        verify(agendamentoRepository, never()).findById(any());
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    public void deveFalharSeJWTForInvalido(){
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getDetails()).thenReturn("usuario-invalido");

        assertThrows(IllegalAccessException.class, () ->
                agendamentoService.cancelarAgendamento(1L, authentication));

        verify(agendamentoRepository, never()).findById(anyLong());
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    public void deveFalharQuandoAgendamentoNaoForEncontrado(){
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                agendamentoService.cancelarAgendamento(1L, authentication));

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    public void deveFalharQuandoOutroUsuarioTentarCancelarAgendamentoDeOutroUsuario(){
        UUID outroClient = UUID.randomUUID();

        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setClienteId(outroClient);
        agendamento.setProfissionalId(outroClient);
        agendamento.setStatus(AgendamentoStatus.CONFIRMADO);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        assertThrows(IllegalAccessException.class, () ->
                agendamentoService.cancelarAgendamento(1L, authentication));
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    public void naoDeveCancelarAgendamentoQuandoStatusForConcluido(){
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setClienteId(clienteId);
        agendamento.setStatus(AgendamentoStatus.CONCLUIDO);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        assertThrows(IllegalArgumentException.class, () ->
                agendamentoService.cancelarAgendamento(1L, authentication));
        verify(agendamentoRepository, never()).save(any());

    }

    @Test
    public void naoDeveCancelarAgendamentoComStatusDeCancelado(){
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setClienteId(clienteId);
        agendamento.setStatus(AgendamentoStatus.CANCELADO);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        assertThrows(IllegalArgumentException.class, () ->
                agendamentoService.cancelarAgendamento(1L, authentication));
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void ct154_deveAtualizarAgendamentoValido() {
        configurarRequestComBearerToken("token-admin");

        Agendamento existente = new Agendamento();
        existente.setId(1L);
        existente.setClienteId(clienteId);
        existente.setProfissionalId(profissionalId);
        existente.setServicoId(1L);
        existente.setDataHoraInicio(LocalDateTime.of(2026, 4, 10, 10, 0));
        existente.setDataHoraFim(LocalDateTime.of(2026, 4, 10, 11, 0));
        existente.setStatus(AgendamentoStatus.CONFIRMADO);

        LocalDateTime novoInicio = LocalDateTime.of(2026, 4, 11, 14, 0);
        AgendamentoDTO dto = dtoAtualizacao(clienteId, profissionalId, 2L, novoInicio, AgendamentoStatus.CONFIRMADO);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(servicoClient.getServicoById(eq(2L), eq("token-admin"))).thenReturn(ResponseEntity.ok(servicoComDuracao(60)));
        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndIdNotAndStatusNot(
                eq(profissionalId), eq(novoInicio), eq(novoInicio.plusMinutes(60)), eq(1L), eq(AgendamentoStatus.CANCELADO)
        )).thenReturn(List.of());
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoDTO resultado = agendamentoService.atualizarAgendamento(1L, dto, authentication);

        assertEquals(1L, resultado.getId());
        assertEquals(2L, resultado.getServicoId());
        assertEquals(novoInicio, resultado.getDataHoraInicio());
        assertEquals(novoInicio.plusMinutes(60), resultado.getDataHoraFim());

        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    @Test
    void ct155_deveFalharAoAtualizarAgendamentoInexistente() {
        AgendamentoDTO dto = dtoAtualizacao(clienteId, profissionalId, 2L, LocalDateTime.of(2026, 4, 11, 14, 0), AgendamentoStatus.CONFIRMADO);

        when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> agendamentoService.atualizarAgendamento(99L, dto, authentication));

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void ct156_deveFalharAoAtualizarAgendamentoComConflito() {
        configurarRequestComBearerToken("token-admin");

        Agendamento existente = new Agendamento();
        existente.setId(1L);

        LocalDateTime inicio = LocalDateTime.of(2026, 4, 11, 14, 0);
        AgendamentoDTO dto = dtoAtualizacao(clienteId, profissionalId, 2L, inicio, AgendamentoStatus.CONFIRMADO);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(servicoClient.getServicoById(eq(2L), eq("token-admin"))).thenReturn(ResponseEntity.ok(servicoComDuracao(60)));
        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndIdNotAndStatusNot(
                eq(profissionalId), eq(inicio), eq(inicio.plusMinutes(60)), eq(1L), eq(AgendamentoStatus.CANCELADO)
        )).thenReturn(List.of(new Agendamento()));

        assertThrows(IllegalArgumentException.class,
                () -> agendamentoService.atualizarAgendamento(1L, dto, authentication));

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void ct157_deveListarTodosAgendamentos() {
        when(agendamentoRepository.findAll()).thenReturn(List.of(agendamento));

        List<AgendamentoDTO> resultado = agendamentoService.listarTodosAgendamentos();

        assertEquals(1, resultado.size());
        assertEquals(id, resultado.get(0).getId());

        verify(agendamentoRepository).findAll();
    }

    @Test
    void ct158_deveBuscarAgendamentoPorIdExistente() {
        when(agendamentoRepository.findById(id)).thenReturn(Optional.of(agendamento));

        AgendamentoDTO resultado = agendamentoService.buscarAgendamentoPorId(id);

        assertEquals(id, resultado.getId());
        assertEquals(clienteId, resultado.getClienteId());

        verify(agendamentoRepository).findById(id);
    }

    @Test
    void ct159_deveFalharAoBuscarAgendamentoInexistente() {
        when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> agendamentoService.buscarAgendamentoPorId(99L));
    }

    @Test
    void ct160_deveExcluirAgendamentoExistente() {
        when(agendamentoRepository.existsById(1L)).thenReturn(true);

        agendamentoService.excluirAgendamento(1L);

        verify(agendamentoRepository).deleteById(1L);
    }

    @Test
    void ct161_deveFalharAoExcluirAgendamentoInexistente() {
        when(agendamentoRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> agendamentoService.excluirAgendamento(99L));

        verify(agendamentoRepository, never()).deleteById(anyLong());
    }

    @Test
    void ct162_deveBuscarHorariosComTodosSlotsLivres() {
        configurarRequestComBearerToken("token-user");

        LocalDate data = LocalDate.of(2026, 4, 10);

        when(servicoClient.getServicoById(eq(1L), eq("token-user"))).thenReturn(ResponseEntity.ok(servicoComDuracao(60)));
        when(agendamentoRepository.findByProfissionalIdAndDataHoraInicioBetweenAndStatusNot(
                eq(profissionalId), any(), any(), eq(AgendamentoStatus.CANCELADO)
        )).thenReturn(List.of());
        when(bloqueioHorarioService.isPeriodoDisponivelParaAgendamento(eq(profissionalId), any(), any(), isNull()))
                .thenReturn(true);

        DisponibilidadeDTO resultado =
                agendamentoService.buscarHorariosDisponiveis(data, 1L, profissionalId, authentication);

        assertEquals(data, resultado.getData());
        assertTrue(resultado.getHorariosDisponiveis().contains("09:00"));
        assertTrue(resultado.getHorariosDisponiveis().contains("17:00"));
    }

    @Test
    void ct163_deveRemoverSlotComConflitoDeAgendamento() {
        configurarRequestComBearerToken("token-user");

        LocalDate data = LocalDate.of(2026, 4, 10);

        Agendamento ocupado = new Agendamento();
        ocupado.setDataHoraInicio(LocalDateTime.of(2026, 4, 10, 10, 0));
        ocupado.setDataHoraFim(LocalDateTime.of(2026, 4, 10, 11, 0));
        ocupado.setStatus(AgendamentoStatus.CONFIRMADO);

        when(servicoClient.getServicoById(eq(1L), eq("token-user"))).thenReturn(ResponseEntity.ok(servicoComDuracao(60)));
        when(agendamentoRepository.findByProfissionalIdAndDataHoraInicioBetweenAndStatusNot(
                eq(profissionalId), any(), any(), eq(AgendamentoStatus.CANCELADO)
        )).thenReturn(List.of(ocupado));
        when(bloqueioHorarioService.isPeriodoDisponivelParaAgendamento(eq(profissionalId), any(), any(), isNull()))
                .thenReturn(true);

        DisponibilidadeDTO resultado =
                agendamentoService.buscarHorariosDisponiveis(data, 1L, profissionalId, authentication);

        assertFalse(resultado.getHorariosDisponiveis().contains("10:00"));
        assertTrue(resultado.getHorariosDisponiveis().contains("11:00"));
    }

    @Test
    void ct164_deveRemoverSlotComBloqueioDeHorario() {
        configurarRequestComBearerToken("token-user");

        LocalDate data = LocalDate.of(2026, 4, 10);

        when(servicoClient.getServicoById(eq(1L), eq("token-user"))).thenReturn(ResponseEntity.ok(servicoComDuracao(60)));
        when(agendamentoRepository.findByProfissionalIdAndDataHoraInicioBetweenAndStatusNot(
                eq(profissionalId), any(), any(), eq(AgendamentoStatus.CANCELADO)
        )).thenReturn(List.of());

        when(bloqueioHorarioService.isPeriodoDisponivelParaAgendamento(
                eq(profissionalId),
                eq(LocalDateTime.of(2026, 4, 10, 9, 0)),
                eq(LocalDateTime.of(2026, 4, 10, 10, 0)),
                isNull()
        )).thenReturn(false);

        when(bloqueioHorarioService.isPeriodoDisponivelParaAgendamento(
                eq(profissionalId),
                argThat(inicio -> !inicio.equals(LocalDateTime.of(2026, 4, 10, 9, 0))),
                any(),
                isNull()
        )).thenReturn(true);

        DisponibilidadeDTO resultado =
                agendamentoService.buscarHorariosDisponiveis(data, 1L, profissionalId, authentication);

        assertFalse(resultado.getHorariosDisponiveis().contains("09:00"));
        assertTrue(resultado.getHorariosDisponiveis().contains("10:00"));
    }

    @Test
    void ct165_deveListarAgendamentosDoClientePeloToken() {
        configurarRequestComBearerToken("token-user");

        when(jwtService.extractUserId("token-user"))
                .thenReturn(clienteId);

        when(agendamentoRepository.findByClienteId(clienteId))
                .thenReturn(List.of(agendamento));

        List<AgendamentoDTO> resultado =
                agendamentoService.listarAgendamentosPorCliente(authentication);

        assertEquals(1, resultado.size());
        assertEquals(id, resultado.get(0).getId());
        assertEquals(clienteId, resultado.get(0).getClienteId());

        verify(jwtService).extractUserId("token-user");
        verify(agendamentoRepository).findByClienteId(clienteId);
    }
    @Test
    void ct166_deveFalharAoListarAgendamentosSemTokenNaRequisicao() {
        RequestContextHolder.resetRequestAttributes();

        assertThrows(IllegalStateException.class,
                () -> agendamentoService.listarAgendamentosPorCliente(authentication));

        verify(jwtService, never()).extractUserId(anyString());
        verify(agendamentoRepository, never()).findByClienteId(any());
    }
    @Test
    void ct167_deveCriarAgendamentoPeloAdminComSucesso() {
        Jwt jwtAdmin = Jwt.withTokenValue("token-admin")
                .header("alg", "none")
                .subject(UUID.randomUUID().toString())
                .build();

        Authentication adminAuthentication = mock(Authentication.class);
        when(adminAuthentication.getPrincipal()).thenReturn(jwtAdmin);

        LocalDateTime inicio = LocalDateTime.of(2026, 4, 12, 15, 0);

        AgendamentoDTO dto = dtoAtualizacao(
                clienteId,
                profissionalId,
                1L,
                inicio,
                AgendamentoStatus.CONFIRMADO
        );

        when(servicoClient.getServicoById(eq(1L), eq("Bearer token-admin")))
                .thenReturn(ResponseEntity.ok(servicoComDuracao(60)));

        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndStatusNot(
                eq(profissionalId),
                eq(inicio),
                eq(inicio.plusMinutes(60)),
                eq(AgendamentoStatus.CANCELADO)
        )).thenReturn(List.of());

        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(invocation -> {
                    Agendamento salvo = invocation.getArgument(0);
                    salvo.setId(20L);
                    return salvo;
                });

        when(authService.getEmailPorId(any(UUID.class)))
                .thenReturn("teste@email.com");

        AgendamentoDTO resultado =
                agendamentoService.salvarAgendamentoPeloAdmin(dto, adminAuthentication);

        assertEquals(20L, resultado.getId());
        assertEquals(clienteId, resultado.getClienteId());
        assertEquals(profissionalId, resultado.getProfissionalId());
        assertEquals(inicio, resultado.getDataHoraInicio());
        assertEquals(inicio.plusMinutes(60), resultado.getDataHoraFim());

        verify(agendamentoRepository).save(any(Agendamento.class));
        verify(emailPublisher).publishEmail(any(EmailDto.class));
    }
    @Test
    void ct168_deveFalharAoCriarAgendamentoPeloAdminComConflito() {
        Jwt jwtAdmin = Jwt.withTokenValue("token-admin")
                .header("alg", "none")
                .subject(UUID.randomUUID().toString())
                .build();

        Authentication adminAuthentication = mock(Authentication.class);
        when(adminAuthentication.getPrincipal()).thenReturn(jwtAdmin);

        LocalDateTime inicio = LocalDateTime.of(2026, 4, 12, 15, 0);

        AgendamentoDTO dto = dtoAtualizacao(
                clienteId,
                profissionalId,
                1L,
                inicio,
                AgendamentoStatus.CONFIRMADO
        );

        when(servicoClient.getServicoById(eq(1L), eq("Bearer token-admin")))
                .thenReturn(ResponseEntity.ok(servicoComDuracao(60)));

        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndStatusNot(
                eq(profissionalId),
                eq(inicio),
                eq(inicio.plusMinutes(60)),
                eq(AgendamentoStatus.CANCELADO)
        )).thenReturn(List.of(new Agendamento()));

        assertThrows(IllegalArgumentException.class,
                () -> agendamentoService.salvarAgendamentoPeloAdmin(dto, adminAuthentication));

        verify(agendamentoRepository, never()).save(any());
        verify(emailPublisher, never()).publishEmail(any());
    }
    @Test
    void ct169_deveAtualizarAgendamentoPeloAdminComSucesso() {
        Jwt jwtAdmin = Jwt.withTokenValue("token-admin")
                .header("alg", "none")
                .subject(UUID.randomUUID().toString())
                .build();

        Authentication adminAuthentication = mock(Authentication.class);
        when(adminAuthentication.getPrincipal()).thenReturn(jwtAdmin);

        Agendamento existente = new Agendamento();
        existente.setId(1L);
        existente.setClienteId(clienteId);
        existente.setProfissionalId(profissionalId);
        existente.setServicoId(1L);
        existente.setDataHoraInicio(LocalDateTime.of(2026, 4, 10, 10, 0));
        existente.setDataHoraFim(LocalDateTime.of(2026, 4, 10, 11, 0));
        existente.setStatus(AgendamentoStatus.CONFIRMADO);

        LocalDateTime novoInicio = LocalDateTime.of(2026, 4, 13, 16, 0);

        AgendamentoDTO dto = dtoAtualizacao(
                clienteId,
                profissionalId,
                2L,
                novoInicio,
                AgendamentoStatus.CONFIRMADO
        );

        when(agendamentoRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        when(servicoClient.getServicoById(eq(2L), eq("Bearer token-admin")))
                .thenReturn(ResponseEntity.ok(servicoComDuracao(90)));

        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndIdNotAndStatusNot(
                eq(profissionalId),
                eq(novoInicio),
                eq(novoInicio.plusMinutes(90)),
                eq(1L),
                eq(AgendamentoStatus.CANCELADO)
        )).thenReturn(List.of());

        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoDTO resultado =
                agendamentoService.atualizarAgendamentoPeloAdmin(1L, dto, adminAuthentication);

        assertEquals(1L, resultado.getId());
        assertEquals(2L, resultado.getServicoId());
        assertEquals(novoInicio, resultado.getDataHoraInicio());
        assertEquals(novoInicio.plusMinutes(90), resultado.getDataHoraFim());

        verify(agendamentoRepository).save(any(Agendamento.class));
    }
    @Test
    void ct170_deveFalharAoAtualizarAgendamentoPeloAdminInexistente() {
        Authentication adminAuthentication = mock(Authentication.class);

        AgendamentoDTO dto = dtoAtualizacao(
                clienteId,
                profissionalId,
                2L,
                LocalDateTime.of(2026, 4, 13, 16, 0),
                AgendamentoStatus.CONFIRMADO
        );

        when(agendamentoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> agendamentoService.atualizarAgendamentoPeloAdmin(99L, dto, adminAuthentication));

        verify(adminAuthentication, never()).getPrincipal();
        verify(agendamentoRepository, never()).save(any());
    }
    @Test
    void ct171_deveFalharAoAtualizarAgendamentoPeloAdminComConflito() {
        Jwt jwtAdmin = Jwt.withTokenValue("token-admin")
                .header("alg", "none")
                .subject(UUID.randomUUID().toString())
                .build();

        Authentication adminAuthentication = mock(Authentication.class);
        when(adminAuthentication.getPrincipal()).thenReturn(jwtAdmin);

        Agendamento existente = new Agendamento();
        existente.setId(1L);

        LocalDateTime novoInicio = LocalDateTime.of(2026, 4, 13, 16, 0);

        AgendamentoDTO dto = dtoAtualizacao(
                clienteId,
                profissionalId,
                2L,
                novoInicio,
                AgendamentoStatus.CONFIRMADO
        );

        when(agendamentoRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        when(servicoClient.getServicoById(eq(2L), eq("Bearer token-admin")))
                .thenReturn(ResponseEntity.ok(servicoComDuracao(90)));

        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndIdNotAndStatusNot(
                eq(profissionalId),
                eq(novoInicio),
                eq(novoInicio.plusMinutes(90)),
                eq(1L),
                eq(AgendamentoStatus.CANCELADO)
        )).thenReturn(List.of(new Agendamento()));

        assertThrows(IllegalArgumentException.class,
                () -> agendamentoService.atualizarAgendamentoPeloAdmin(1L, dto, adminAuthentication));

        verify(agendamentoRepository, never()).save(any());
    }
    @Test
    void ct172_deveSalvarAgendamentoAdminMesmoQuandoEnvioDeEmailFalhar() {
        Jwt jwtAdmin = Jwt.withTokenValue("token-admin")
                .header("alg", "none")
                .subject(UUID.randomUUID().toString())
                .build();

        Authentication adminAuthentication = mock(Authentication.class);
        when(adminAuthentication.getPrincipal()).thenReturn(jwtAdmin);

        LocalDateTime inicio = LocalDateTime.of(2026, 4, 12, 15, 0);

        AgendamentoDTO dto = dtoAtualizacao(
                clienteId,
                profissionalId,
                1L,
                inicio,
                AgendamentoStatus.CONFIRMADO
        );

        when(servicoClient.getServicoById(eq(1L), eq("Bearer token-admin")))
                .thenReturn(ResponseEntity.ok(servicoComDuracao(60)));

        when(agendamentoRepository.findByProfissionalIdAndDataHoraFimAfterAndDataHoraInicioBeforeAndStatusNot(
                eq(profissionalId),
                eq(inicio),
                eq(inicio.plusMinutes(60)),
                eq(AgendamentoStatus.CANCELADO)
        )).thenReturn(List.of());

        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(invocation -> {
                    Agendamento salvo = invocation.getArgument(0);
                    salvo.setId(20L);
                    return salvo;
                });

        when(authService.getEmailPorId(any(UUID.class)))
                .thenReturn("teste@email.com");

        doThrow(new RuntimeException("falha no email"))
                .when(emailPublisher).publishEmail(any(EmailDto.class));

        AgendamentoDTO resultado =
                agendamentoService.salvarAgendamentoPeloAdmin(dto, adminAuthentication);

        assertEquals(20L, resultado.getId());

        verify(agendamentoRepository).save(any(Agendamento.class));
        verify(emailPublisher).publishEmail(any(EmailDto.class));
    }


    private AgendamentoDTO dtoAtualizacao(
            UUID clienteId,
            UUID profissionalId,
            Long servicoId,
            LocalDateTime inicio,
            AgendamentoStatus status
    ) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setClienteId(clienteId);
        dto.setProfissionalId(profissionalId);
        dto.setServicoId(servicoId);
        dto.setDataHoraInicio(inicio);
        dto.setStatus(status);
        dto.setObservacoes("Atualizado");
        return dto;
    }

    private ServicoDTOForAgendamento servicoComDuracao(int duracao) {
        ServicoDTOForAgendamento servico = new ServicoDTOForAgendamento();
        servico.setNome("Manicure");
        servico.setDuracao(duracao);
        return servico;
    }

    private void configurarRequestComBearerToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

}
