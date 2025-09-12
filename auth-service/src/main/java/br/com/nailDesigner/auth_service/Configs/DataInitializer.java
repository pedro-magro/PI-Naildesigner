package br.com.nailDesigner.auth_service.Configs;

import br.com.nailDesigner.auth_service.Models.Role;
import br.com.nailDesigner.auth_service.Models.User;
import br.com.nailDesigner.auth_service.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=========================================================");
        System.out.println(">>>> [DataInitializer] EXECUTANDO: Verificando usuários...");

        // Usuário 1: "usuariojwt"
        if (userRepository.findByUsername("usuariojwt").isEmpty()) {
            User user = new User();
            user.setUsername("usuariojwt");
            user.setEmail("jwt@exemplo.com");
            user.setPassword(passwordEncoder.encode("senhaForte123"));
            user.setRole(Role.USER);
            user.setPhone("11911112222");
            userRepository.save(user);
            System.out.println(">>>> Usuário de teste 'usuariojwt' criado!");
        }

        // Usuário 2: "admin"
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@exemplo.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setPhone("11900000000");
            userRepository.save(admin);
            System.out.println(">>>> Usuário de teste 'admin' criado!");
        }
        
        // <<--- REVERTIDO: Usuário "João de Deus" com espaços no username --->>
        // A busca por e-mail deve continuar a funcionar independentemente.
        if (userRepository.findByUsername("João de Deus").isEmpty()) {
            User joao = new User();
            joao.setUsername("João de Deus"); 
            joao.setEmail("Joaodedeus@socorro.com.br");
            joao.setPassword(passwordEncoder.encode("dedeus"));
            joao.setRole(Role.USER);
            joao.setPhone("1199999000");
            userRepository.save(joao);
            System.out.println(">>>> Usuário de teste 'João de Deus' criado!");
        }

        System.out.println("---------------------------------------------------------");
        System.out.println("[DataInitializer] VERIFICAÇÃO DO BANCO DE DADOS APÓS A INICIALIZAÇÃO:");
        userRepository.findAll().forEach(u -> 
            System.out.println(" -> No DB: username='" + u.getUsername() + "', email='" + u.getEmail() + "'")
        );
        System.out.println("=========================================================");
    }
}
