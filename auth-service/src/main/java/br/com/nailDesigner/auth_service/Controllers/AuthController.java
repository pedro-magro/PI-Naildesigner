package br.com.nailDesigner.auth_service.Controllers;

import br.com.nailDesigner.auth.api.Dtos.ProfissionalDto;
import br.com.nailDesigner.auth.api.Dtos.UpdateProfileRequestDto;
import br.com.nailDesigner.auth.api.Dtos.UserProfileDto;
import br.com.nailDesigner.auth_service.Models.Role;
import br.com.nailDesigner.auth_service.Models.User;
import br.com.nailDesigner.auth_service.Repositories.UserRepository;
import br.com.nailDesigner.auth_service.Services.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    // DTOs
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String token) {}
    public record RegisterRequest(
        @NotBlank @Size(min = 3) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @NotBlank (message = "Telefone é obrigatório") String phone
    ) {}
    public record AdminUserResponse(UUID id, String username, String email, String phone, String role) {}

    private AdminUserResponse toAdminUserResponse(User user) {
        return new AdminUserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhone(),
            user.getRole().name()
        );
    }

 // Dentro da sua classe AuthController.java

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // O AuthenticationManager usará seu UserDetailsServiceImpl para encontrar o usuário
            // por e-mail ou username e validar a senha.
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            if (authentication.isAuthenticated()) {
                // Após a autenticação, o "Principal" é o seu objeto User (UserDetails).
                // Podemos fazer um cast para pegar os detalhes completos.
                User authenticatedUser = (User) authentication.getPrincipal();

                // Geramos o token usando o objeto User que já foi carregado e autenticado.
                // Não precisamos buscar no banco de dados novamente!
                String token = jwtService.generateToken(authenticatedUser);
                
                return ResponseEntity.ok(new LoginResponse(token));
            } else {
                // Este caso é raro, mas é bom tê-lo por segurança.
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        } catch (AuthenticationException e) {
            // Se a autenticação falhar (usuário não encontrado, senha errada),
            // o authenticationManager lança uma exceção.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username já existe.");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já existe.");
        }
        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(Role.USER); // Papel padrão para novos registros
        newUser.setPhone(request.phone());
        
        userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso!");
    }
    
 // Dentro da classe AuthController.java

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(Authentication authentication) {
        // Verifica se a autenticação é válida e se o principal é uma instância do seu UserDetails
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            // Se não estiver autenticado ou o principal não for do tipo esperado, retorna UNAUTHORIZED
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
     // Casting para o seu objeto User
        User currentUser = (User) authentication.getPrincipal(); 
        
        // Coleta as roles (autoridades) do usuário autenticado
        // O método getAuthorities() é do UserDetails (implementado pelo seu User model)
        List<String> roles = currentUser.getAuthorities().stream()
                               .map(authority -> authority.getAuthority())
                               .collect(Collectors.toList());

        UserProfileDto userProfile = new UserProfileDto(
            currentUser.getUsername(),
            currentUser.getEmail(),
            currentUser.getPhone(), 
            roles
        );
        
        return ResponseEntity.ok(userProfile);
    }
    
    // --- CORREÇÃO APLICADA AQUI ---
    @PutMapping("/me")
    public ResponseEntity<String> updateCurrentUser(Authentication authentication, @RequestBody UpdateProfileRequestDto updateRequest) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User userAuthenticated = (User) authentication.getPrincipal();
        UUID userId = userAuthenticated.getId(); // Pega o ID do usuário autenticado
        
        User userToUpdate = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para atualização."));

        boolean isUpdated = false;

        if (updateRequest.username() != null && !updateRequest.username().isBlank() && !updateRequest.username().equals(userToUpdate.getUsername())) {
            if(userRepository.findByUsername(updateRequest.username()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Este nome de usuário já está em uso.");
            }
            userToUpdate.setUsername(updateRequest.username());
            isUpdated = true;
        }
        if (updateRequest.phone() != null && !updateRequest.phone().isBlank()) {
            userToUpdate.setPhone(updateRequest.phone());
            isUpdated = true;
        }
        if (updateRequest.password() != null && !updateRequest.password().isBlank()) {
            userToUpdate.setPassword(passwordEncoder.encode(updateRequest.password()));
            isUpdated = true;
        }

        if (isUpdated) {
            userRepository.save(userToUpdate);
            return ResponseEntity.ok("Perfil atualizado com sucesso!");
        }

        return ResponseEntity.ok("Nenhuma alteração foi fornecida.");
    }
    
    @GetMapping("/profissionais")
    public ResponseEntity<List<ProfissionalDto>> getProfissionais() {
        // Busca todos os usuários que têm a role de ADMIN
        List<User> profissionais = userRepository.findByRole(Role.ADMIN);
        
        // Converte a lista de User para uma lista de ProfissionalDto
        List<ProfissionalDto> dtoList = profissionais.stream()
            .map(p -> new ProfissionalDto(p.getId(), p.getUsername())) // Usando getUsername() para o nome
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(dtoList);
    }
    @GetMapping("/users/{id}/email")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> getUserEmailById(@PathVariable UUID id, Authentication authentication) {
        // A segurança é garantida pela cadeia de filtros que exige um token válido.
        // Opcional: Adicionar lógica para verificar se o chamador tem permissão (ex: é um ADMIN ou o próprio usuário).
        
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(user.getEmail())) // Se encontrar, retorna o email com status 200 OK.
                .orElse(ResponseEntity.notFound().build());      // Se não encontrar, retorna 404 Not Found.
    }
    
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(
            userRepository.findAll().stream().map(this::toAdminUserResponse).toList()
        );
    }

    /**
     * Busca um usuário específico por ID. Apenas para ADMIN.
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
            .map(this::toAdminUserResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username já existe.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já existe.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(toAdminUserResponse(savedUser), HttpStatus.CREATED);
    }

    /**
     * Atualiza um usuário existente. Apenas para ADMIN.
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            if (!user.getUsername().equals(userDetails.getUsername())
                && userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username já existe.");
            }
            if (!user.getEmail().equals(userDetails.getEmail())
                && userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já existe.");
            }
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            user.setRole(userDetails.getRole());
            // Apenas atualiza a senha se uma nova for fornecida
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            return ResponseEntity.ok(toAdminUserResponse(userRepository.save(user)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deleta um usuário. Apenas para ADMIN.
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
}
