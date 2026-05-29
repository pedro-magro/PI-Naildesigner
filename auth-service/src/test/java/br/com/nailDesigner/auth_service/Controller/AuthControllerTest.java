package br.com.nailDesigner.auth_service.Controller;

import br.com.nailDesigner.auth.api.Dtos.ProfissionalDto;
import br.com.nailDesigner.auth.api.Dtos.UpdateProfileRequestDto;
import br.com.nailDesigner.auth.api.Dtos.UserProfileDto;
import br.com.nailDesigner.auth_service.Controllers.AuthController;
import br.com.nailDesigner.auth_service.Models.Role;
import br.com.nailDesigner.auth_service.Models.User;
import br.com.nailDesigner.auth_service.Repositories.UserRepository;
import br.com.nailDesigner.auth_service.Services.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void ct117_deveRetornarTokenQuandoLoginForValido() {
        AuthController.LoginRequest request =
                new AuthController.LoginRequest("pedro", "senha123");

        User usuarioAutenticado = new User(
                "pedro",
                "senhaCriptografada",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getPrincipal())
                .thenReturn(usuarioAutenticado);

        when(jwtService.generateToken(usuarioAutenticado))
                .thenReturn("token-jwt-valido");

        ResponseEntity<AuthController.LoginResponse> response =
                authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("token-jwt-valido", response.getBody().token());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(captor.capture());

        UsernamePasswordAuthenticationToken tokenAutenticacao = captor.getValue();

        assertEquals("pedro", tokenAutenticacao.getPrincipal());
        assertEquals("senha123", tokenAutenticacao.getCredentials());

        verify(jwtService).generateToken(usuarioAutenticado);
    }

    @Test
    void ct118_deveRetornarUnauthorizedQuandoLoginForInvalido() {
        AuthController.LoginRequest request =
                new AuthController.LoginRequest("pedro", "senhaErrada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        ResponseEntity<AuthController.LoginResponse> response =
                authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void ct119_deveCadastrarNovoUsuarioComSucesso() {
        AuthController.RegisterRequest request =
                new AuthController.RegisterRequest(
                        "pedro",
                        "pedro@email.com",
                        "senha123",
                        "11999999999"
                );

        when(userRepository.findByUsername("pedro"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("pedro@email.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("senha123"))
                .thenReturn("senhaCriptografada");

        ResponseEntity<String> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Usuário registrado com sucesso!", response.getBody());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User usuarioSalvo = captor.getValue();

        assertEquals("pedro", usuarioSalvo.getUsername());
        assertEquals("pedro@email.com", usuarioSalvo.getEmail());
        assertEquals("senhaCriptografada", usuarioSalvo.getPassword());
        assertEquals("11999999999", usuarioSalvo.getPhone());
        assertEquals(Role.USER, usuarioSalvo.getRole());

        verify(passwordEncoder).encode("senha123");
    }

    @Test
    void ct120_deveRetornarConflictQuandoUsernameJaExiste() {
        AuthController.RegisterRequest request =
                new AuthController.RegisterRequest(
                        "pedro",
                        "pedro@email.com",
                        "senha123",
                        "11999999999"
                );

        User usuarioExistente = new User(
                "pedro",
                "senhaCriptografada",
                "outro@email.com",
                "11888888888",
                Role.USER
        );

        when(userRepository.findByUsername("pedro"))
                .thenReturn(Optional.of(usuarioExistente));

        ResponseEntity<String> response = authController.register(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username já existe.", response.getBody());

        verify(userRepository).findByUsername("pedro");
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void ct121_deveRetornarConflictQuandoEmailJaExiste() {
        AuthController.RegisterRequest request =
                new AuthController.RegisterRequest(
                        "pedro",
                        "pedro@email.com",
                        "senha123",
                        "11999999999"
                );

        User usuarioExistente = new User(
                "outroUsuario",
                "senhaCriptografada",
                "pedro@email.com",
                "11888888888",
                Role.USER
        );

        when(userRepository.findByUsername("pedro"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("pedro@email.com"))
                .thenReturn(Optional.of(usuarioExistente));

        ResponseEntity<String> response = authController.register(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email já existe.", response.getBody());

        verify(userRepository).findByUsername("pedro");
        verify(userRepository).findByEmail("pedro@email.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void ct122_deveRetornarPerfilDoUsuarioAutenticado() {
        User usuarioAutenticado = new User(
                "pedro",
                "senhaCriptografada",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getPrincipal())
                .thenReturn(usuarioAutenticado);

        ResponseEntity<UserProfileDto> response =
                authController.getCurrentUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        UserProfileDto perfil = response.getBody();

        assertEquals("pedro", perfil.username());
        assertEquals("pedro@email.com", perfil.email());
        assertEquals("11999999999", perfil.phone());
        assertTrue(perfil.roles().contains("ROLE_USER"));
    }

    @Test
    void ct123_deveRetornarUnauthorizedQuandoAuthenticationForNull() {
        ResponseEntity<UserProfileDto> response =
                authController.getCurrentUser(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct124_deveRetornarUnauthorizedQuandoPrincipalNaoForUser() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getPrincipal())
                .thenReturn("principal-invalido");

        ResponseEntity<UserProfileDto> response =
                authController.getCurrentUser(authentication);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void ct125_deveRetornarUnauthorizedQuandoAuthenticationNaoEstiverAutenticado() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated())
                .thenReturn(false);

        ResponseEntity<UserProfileDto> response =
                authController.getCurrentUser(authentication);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        verify(authentication, never()).getPrincipal();
    }

    @Test
    void ct126_deveAtualizarPerfilComDadosValidos() {
        User usuarioAutenticado = new User(
                "pedro",
                "senhaAntiga",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        User usuarioBanco = usuarioAutenticado;

        Authentication authentication = mock(Authentication.class);

        UpdateProfileRequestDto updateRequest =
                new UpdateProfileRequestDto(
                        "pedro_novo",
                        "11888888888",
                        "novaSenha123"
                );

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getPrincipal())
                .thenReturn(usuarioAutenticado);

        when(userRepository.findById(usuarioAutenticado.getId()))
                .thenReturn(Optional.of(usuarioBanco));

        when(userRepository.findByUsername("pedro_novo"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("novaSenha123"))
                .thenReturn("novaSenhaCriptografada");

        ResponseEntity<String> response =
                authController.updateCurrentUser(authentication, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Perfil atualizado com sucesso!", response.getBody());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User usuarioSalvo = captor.getValue();

        assertEquals("pedro_novo", usuarioSalvo.getUsername());
        assertEquals("11888888888", usuarioSalvo.getPhone());
        assertEquals("novaSenhaCriptografada", usuarioSalvo.getPassword());

        verify(passwordEncoder).encode("novaSenha123");
    }

    @Test
    void ct127_deveRetornarConflictQuandoAtualizarPerfilComUsernameDuplicado() {
        User usuarioAutenticado = new User(
                "pedro",
                "senhaAntiga",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        User usuarioComUsernameDuplicado = new User(
                "pedro_novo",
                "outraSenha",
                "outro@email.com",
                "11888888888",
                Role.USER
        );

        Authentication authentication = mock(Authentication.class);

        UpdateProfileRequestDto updateRequest =
                new UpdateProfileRequestDto(
                        "pedro_novo",
                        "11777777777",
                        "novaSenha123"
                );

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getPrincipal())
                .thenReturn(usuarioAutenticado);

        when(userRepository.findById(usuarioAutenticado.getId()))
                .thenReturn(Optional.of(usuarioAutenticado));

        when(userRepository.findByUsername("pedro_novo"))
                .thenReturn(Optional.of(usuarioComUsernameDuplicado));

        ResponseEntity<String> response =
                authController.updateCurrentUser(authentication, updateRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Este nome de usuário já está em uso.", response.getBody());

        verify(userRepository).findByUsername("pedro_novo");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void ct128_deveRetornarMensagemQuandoNenhumaAlteracaoForFornecida() {
        User usuarioAutenticado = new User(
                "pedro",
                "senhaAntiga",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        Authentication authentication = mock(Authentication.class);

        UpdateProfileRequestDto updateRequest =
                new UpdateProfileRequestDto(
                        "pedro",
                        "",
                        ""
                );

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getPrincipal())
                .thenReturn(usuarioAutenticado);

        when(userRepository.findById(usuarioAutenticado.getId()))
                .thenReturn(Optional.of(usuarioAutenticado));

        ResponseEntity<String> response =
                authController.updateCurrentUser(authentication, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Nenhuma"));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void ct129A_deveRetornarUnauthorizedAoAtualizarPerfilSemAuthentication() {
        UpdateProfileRequestDto updateRequest =
                new UpdateProfileRequestDto(
                        "pedro_novo",
                        "11888888888",
                        "novaSenha123"
                );

        ResponseEntity<String> response =
                authController.updateCurrentUser(null, updateRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void ct129B_deveRetornarUnauthorizedAoAtualizarPerfilComPrincipalInvalido() {
        Authentication authentication = mock(Authentication.class);

        UpdateProfileRequestDto updateRequest =
                new UpdateProfileRequestDto(
                        "pedro_novo",
                        "11888888888",
                        "novaSenha123"
                );

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getPrincipal())
                .thenReturn("principal-invalido");

        ResponseEntity<String> response =
                authController.updateCurrentUser(authentication, updateRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void ct130_deveListarProfissionaisDisponiveis() {
        User profissional = new User(
                "admin",
                "senha",
                "admin@email.com",
                "11999999999",
                Role.ADMIN
        );

        when(userRepository.findByRole(Role.ADMIN))
                .thenReturn(List.of(profissional));

        ResponseEntity<List<ProfissionalDto>> response =
                authController.getProfissionais();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        ProfissionalDto dto = response.getBody().get(0);

        assertEquals(profissional.getId(), dto.id());
        assertEquals("admin", dto.username());

        verify(userRepository).findByRole(Role.ADMIN);
    }

    @Test
    void ct131_deveBuscarEmailPorIdExistente() {
        User user = new User(
                "pedro",
                "senha",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        Authentication authentication = mock(Authentication.class);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        ResponseEntity<String> response =
                authController.getUserEmailById(user.getId(), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("pedro@email.com", response.getBody());

        verify(userRepository).findById(user.getId());
    }

    @Test
    void ct132_deveRetornarNotFoundAoBuscarEmailPorIdInexistente() {
        UUID id = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        ResponseEntity<String> response =
                authController.getUserEmailById(id, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(userRepository).findById(id);
    }

    @Test
    void ct133_deveListarTodosOsUsuarios() {
        User user = new User(
                "pedro",
                "senha",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        User admin = new User(
                "admin",
                "senha",
                "admin@email.com",
                "11888888888",
                Role.ADMIN
        );

        when(userRepository.findAll())
                .thenReturn(List.of(user, admin));

        ResponseEntity<List<AuthController.AdminUserResponse>> response =
                authController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        assertEquals("pedro", response.getBody().get(0).username());
        assertEquals("USER", response.getBody().get(0).role());

        assertEquals("admin", response.getBody().get(1).username());
        assertEquals("ADMIN", response.getBody().get(1).role());

        verify(userRepository).findAll();
    }

    @Test
    void ct134_deveBuscarUsuarioPorIdExistente() {
        User user = new User(
                "pedro",
                "senha",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        ResponseEntity<AuthController.AdminUserResponse> response =
                authController.getUserById(user.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(user.getId(), response.getBody().id());
        assertEquals("pedro", response.getBody().username());
        assertEquals("pedro@email.com", response.getBody().email());
        assertEquals("11999999999", response.getBody().phone());
        assertEquals("USER", response.getBody().role());

        verify(userRepository).findById(user.getId());
    }

    @Test
    void ct135_deveRetornarNotFoundAoBuscarUsuarioPorIdInexistente() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        ResponseEntity<AuthController.AdminUserResponse> response =
                authController.getUserById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(userRepository).findById(id);
    }

    @Test
    void ct136_deveCriarUsuarioAdminComSucesso() {
        User novoUsuario = new User(
                "novo",
                "senha123",
                "novo@email.com",
                "11777777777",
                Role.USER
        );

        when(userRepository.findByUsername("novo"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("novo@email.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("senha123"))
                .thenReturn("senhaCriptografada");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response =
                authController.createUser(novoUsuario);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthController.AdminUserResponse);

        AuthController.AdminUserResponse body =
                (AuthController.AdminUserResponse) response.getBody();

        assertEquals("novo", body.username());
        assertEquals("novo@email.com", body.email());
        assertEquals("11777777777", body.phone());
        assertEquals("USER", body.role());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User usuarioSalvo = captor.getValue();

        assertEquals("senhaCriptografada", usuarioSalvo.getPassword());

        verify(passwordEncoder).encode("senha123");
    }

    @Test
    void ct137_deveRetornarConflictAoCriarUsuarioComUsernameDuplicado() {
        User novoUsuario = new User(
                "pedro",
                "senha123",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        when(userRepository.findByUsername("pedro"))
                .thenReturn(Optional.of(novoUsuario));

        ResponseEntity<?> response =
                authController.createUser(novoUsuario);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username já existe.", response.getBody());

        verify(userRepository).findByUsername("pedro");
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void ct138_deveRetornarConflictAoCriarUsuarioComEmailDuplicado() {
        User novoUsuario = new User(
                "pedro",
                "senha123",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        User usuarioExistente = new User(
                "outro",
                "senha",
                "pedro@email.com",
                "11888888888",
                Role.USER
        );

        when(userRepository.findByUsername("pedro"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("pedro@email.com"))
                .thenReturn(Optional.of(usuarioExistente));

        ResponseEntity<?> response =
                authController.createUser(novoUsuario);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email já existe.", response.getBody());

        verify(userRepository).findByUsername("pedro");
        verify(userRepository).findByEmail("pedro@email.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void ct139_deveAtualizarUsuarioComSucesso() {
        User usuarioExistente = new User(
                "pedro",
                "senhaAntiga",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        User dadosAtualizados = new User(
                "pedro_admin",
                "novaSenha123",
                "pedro.admin@email.com",
                "11888888888",
                Role.ADMIN
        );

        when(userRepository.findById(usuarioExistente.getId()))
                .thenReturn(Optional.of(usuarioExistente));

        when(userRepository.findByUsername("pedro_admin"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("pedro.admin@email.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("novaSenha123"))
                .thenReturn("novaSenhaCriptografada");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response =
                authController.updateUser(usuarioExistente.getId(), dadosAtualizados);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthController.AdminUserResponse);

        AuthController.AdminUserResponse body =
                (AuthController.AdminUserResponse) response.getBody();

        assertEquals("pedro_admin", body.username());
        assertEquals("pedro.admin@email.com", body.email());
        assertEquals("11888888888", body.phone());
        assertEquals("ADMIN", body.role());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User usuarioSalvo = captor.getValue();

        assertEquals("pedro_admin", usuarioSalvo.getUsername());
        assertEquals("pedro.admin@email.com", usuarioSalvo.getEmail());
        assertEquals("11888888888", usuarioSalvo.getPhone());
        assertEquals(Role.ADMIN, usuarioSalvo.getRole());
        assertEquals("novaSenhaCriptografada", usuarioSalvo.getPassword());
    }

    @Test
    void ct140_deveAtualizarUsuarioSemNovaSenhaMantendoSenhaAtual() {
        User usuarioExistente = new User(
                "pedro",
                "senhaAntiga",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        User dadosAtualizados = new User(
                "pedro_novo",
                "",
                "pedro.novo@email.com",
                "11888888888",
                Role.ADMIN
        );

        when(userRepository.findById(usuarioExistente.getId()))
                .thenReturn(Optional.of(usuarioExistente));

        when(userRepository.findByUsername("pedro_novo"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("pedro.novo@email.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response =
                authController.updateUser(usuarioExistente.getId(), dadosAtualizados);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthController.AdminUserResponse);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User usuarioSalvo = captor.getValue();

        assertEquals("senhaAntiga", usuarioSalvo.getPassword());
        assertEquals("pedro_novo", usuarioSalvo.getUsername());
        assertEquals("pedro.novo@email.com", usuarioSalvo.getEmail());
        assertEquals(Role.ADMIN, usuarioSalvo.getRole());

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void ct141_deveRetornarNotFoundAoAtualizarUsuarioInexistente() {
        UUID id = UUID.randomUUID();

        User dadosAtualizados = new User(
                "pedro",
                "senha",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response =
                authController.updateUser(id, dadosAtualizados);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void ct142_deveRetornarConflictAoAtualizarUsuarioComUsernameDuplicado() {
        User usuarioExistente = new User(
                "pedro",
                "senhaAntiga",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        User dadosAtualizados = new User(
                "username_duplicado",
                "novaSenha",
                "pedro.novo@email.com",
                "11888888888",
                Role.USER
        );

        User usuarioDuplicado = new User(
                "username_duplicado",
                "senha",
                "outro@email.com",
                "11777777777",
                Role.USER
        );

        when(userRepository.findById(usuarioExistente.getId()))
                .thenReturn(Optional.of(usuarioExistente));

        when(userRepository.findByUsername("username_duplicado"))
                .thenReturn(Optional.of(usuarioDuplicado));

        ResponseEntity<?> response =
                authController.updateUser(usuarioExistente.getId(), dadosAtualizados);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username já existe.", response.getBody());

        verify(userRepository).findByUsername("username_duplicado");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void ct143_deveRetornarConflictAoAtualizarUsuarioComEmailDuplicado() {
        User usuarioExistente = new User(
                "pedro",
                "senhaAntiga",
                "pedro@email.com",
                "11999999999",
                Role.USER
        );

        User dadosAtualizados = new User(
                "pedro_novo",
                "novaSenha",
                "email.duplicado@email.com",
                "11888888888",
                Role.USER
        );

        User usuarioDuplicado = new User(
                "outro",
                "senha",
                "email.duplicado@email.com",
                "11777777777",
                Role.USER
        );

        when(userRepository.findById(usuarioExistente.getId()))
                .thenReturn(Optional.of(usuarioExistente));

        when(userRepository.findByUsername("pedro_novo"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("email.duplicado@email.com"))
                .thenReturn(Optional.of(usuarioDuplicado));

        ResponseEntity<?> response =
                authController.updateUser(usuarioExistente.getId(), dadosAtualizados);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email já existe.", response.getBody());

        verify(userRepository).findByUsername("pedro_novo");
        verify(userRepository).findByEmail("email.duplicado@email.com");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void ct144_deveDeletarUsuarioExistente() {
        UUID id = UUID.randomUUID();

        when(userRepository.existsById(id))
                .thenReturn(true);

        ResponseEntity<Void> response =
                authController.deleteUser(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(userRepository).deleteById(id);
    }

    @Test
    void ct145_deveRetornarNotFoundAoDeletarUsuarioInexistente() {
        UUID id = UUID.randomUUID();

        when(userRepository.existsById(id))
                .thenReturn(false);

        ResponseEntity<Void> response =
                authController.deleteUser(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(userRepository, never()).deleteById(id);
    }

}
