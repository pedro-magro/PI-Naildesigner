package br.com.nailDesigner.auth_service.Services;

import br.com.nailDesigner.auth_service.Models.User;
import br.com.nailDesigner.auth_service.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID; // Adicione este import

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        final String login = usernameOrId.trim();
        logger.debug("Tentativa de carregar usuário com o identificador '{}'", login);

        Optional<User> userOptional;

        // ***** LÓGICA CORRIGIDA *****
        // Tenta verificar se a string é um UUID válido
        try {
            UUID id = UUID.fromString(login);
            logger.debug("Identificador recebido é UUID. Buscando por ID.");
            userOptional = userRepository.findById(id);
        } catch (IllegalArgumentException e) {
            // Se não for um UUID, assume que é um username ou email
            if (login.contains("@")) {
                logger.debug("Buscando usuário por email.");
                userOptional = userRepository.findByEmail(login);
            } else {
                logger.debug("Buscando usuário por username.");
                userOptional = userRepository.findByUsername(login);
            }
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            logger.debug("Usuário encontrado: {}", user.getUsername());
            return user;
        } else {
            throw new UsernameNotFoundException("Usuário não encontrado com o identificador: " + login);
        }
    }
}
