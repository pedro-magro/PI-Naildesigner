package br.com.nailDesigner.auth_service.Controller;

import br.com.nailDesigner.auth_service.Configs.CacheConfig;
import br.com.nailDesigner.auth_service.Controllers.AuthController;
import br.com.nailDesigner.auth_service.Models.Role;
import br.com.nailDesigner.auth_service.Models.User;
import br.com.nailDesigner.auth_service.Repositories.UserRepository;
import br.com.nailDesigner.auth_service.Services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = {
    AuthController.class,
    CacheConfig.class,
    AuthControllerCacheTest.TestConfig.class
})
class AuthControllerCacheTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(userRepository);
        if (cacheManager.getCache("profissionais") != null) {
            cacheManager.getCache("profissionais").clear();
        }
    }

    @Test
    void ct241_deveCachearListagemDeProfissionais() {
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of(user("admin", Role.ADMIN)));

        authController.getProfissionais();
        authController.getProfissionais();

        verify(userRepository, times(1)).findByRole(Role.ADMIN);
    }

    @Test
    void ct242_deveInvalidarCacheDeProfissionaisAoAtualizarUsuario() {
        User existente = user("admin", Role.ADMIN);
        User atualizado = user("admin_novo", Role.ADMIN);

        when(userRepository.findByRole(Role.ADMIN))
            .thenReturn(List.of(existente))
            .thenReturn(List.of(atualizado));
        when(userRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(userRepository.findByUsername("admin_novo")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin_novo@email.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authController.getProfissionais();
        authController.updateUser(existente.getId(), atualizado);
        authController.getProfissionais();

        verify(userRepository, times(2)).findByRole(Role.ADMIN);
    }

    private User user(String username, Role role) {
        return new User(username, "senha", username + "@email.com", "11999999999", role);
    }

    @Configuration
    static class TestConfig {
        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        AuthenticationManager authenticationManager() {
            return mock(AuthenticationManager.class);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return mock(PasswordEncoder.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }
    }
}
