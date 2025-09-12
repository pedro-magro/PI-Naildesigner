package br.com.nailDesigner.auth_service.Services;

import br.com.nailDesigner.auth_service.Models.User;
import br.com.nailDesigner.auth_service.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID; // Adicione este import

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        final String login = usernameOrId.trim();

        System.out.println("\n=========================================================");
        System.out.println(">>> [AUTH] Tentativa de carregar usuário com o identificador: '" + login + "'");

        Optional<User> userOptional;

        // ***** LÓGICA CORRIGIDA *****
        // Tenta verificar se a string é um UUID válido
        try {
            UUID id = UUID.fromString(login);
            System.out.println(">>> [AUTH] Identificador é um UUID. Buscando por ID...");
            userOptional = userRepository.findById(id);
        } catch (IllegalArgumentException e) {
            // Se não for um UUID, assume que é um username ou email
            if (login.contains("@")) {
                System.out.println(">>> [AUTH] Buscando por EMAIL...");
                userOptional = userRepository.findByEmail(login);
            } else {
                System.out.println(">>> [AUTH] Buscando por USERNAME...");
                userOptional = userRepository.findByUsername(login);
            }
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println(">>> [AUTH] SUCESSO! Usuário encontrado: " + user.getUsername());
            System.out.println("=========================================================\n");
            return user;
        } else {
            System.out.println(">>> [AUTH] FALHA! Nenhum usuário encontrado com: '" + login + "'");
            System.out.println("=========================================================\n");
            throw new UsernameNotFoundException("Usuário não encontrado com o identificador: " + login);
        }
    }
}