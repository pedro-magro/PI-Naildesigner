package br.com.nailDesigner.auth_service.Configs;

import br.com.nailDesigner.auth_service.Models.Role;
import br.com.nailDesigner.auth_service.Models.User;
import br.com.nailDesigner.auth_service.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${app.seed.user-password:senhaForte123}")
    private String defaultUserPassword;

    @Value("${app.seed.admin-password:Admin123!ChangeMe}")
    private String defaultAdminPassword;

    @Value("${app.seed.demo-password:Demo123!ChangeMe}")
    private String defaultDemoPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            logger.info("Inicialização de usuários desativada via app.seed.enabled=false.");
            return;
        }

        logger.info("Verificando usuários iniciais...");

        // Usuário 1: "usuariojwt"
        if (userRepository.findByUsername("usuariojwt").isEmpty()) {
            User user = new User();
            user.setUsername("usuariojwt");
            user.setEmail("jwt@exemplo.com");
            user.setPassword(passwordEncoder.encode(defaultUserPassword));
            user.setRole(Role.USER);
            user.setPhone("11911112222");
            userRepository.save(user);
            logger.info("Usuário inicial 'usuariojwt' criado.");
        }

        // Usuário 2: "admin"
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@exemplo.com");
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setRole(Role.ADMIN);
            admin.setPhone("11900000000");
            userRepository.save(admin);
            logger.info("Usuário inicial 'admin' criado.");
        }
        
        // <<--- REVERTIDO: Usuário "João de Deus" com espaços no username --->>
        // A busca por e-mail deve continuar a funcionar independentemente.
        if (userRepository.findByUsername("João de Deus").isEmpty()) {
            User joao = new User();
            joao.setUsername("João de Deus"); 
            joao.setEmail("Joaodedeus@socorro.com.br");
            joao.setPassword(passwordEncoder.encode(defaultDemoPassword));
            joao.setRole(Role.USER);
            joao.setPhone("1199999000");
            userRepository.save(joao);
            logger.info("Usuário inicial 'João de Deus' criado.");
        }

        logger.info("Verificação do banco de dados após a inicialização:");
        userRepository.findAll().forEach(u -> 
            logger.info(" -> No DB: username='{}', email='{}'", u.getUsername(), u.getEmail())
        );
    }
}
