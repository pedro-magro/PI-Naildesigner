package br.com.nailDesigner.auth_service.Repositories;

import br.com.nailDesigner.auth_service.Models.Role;
import br.com.nailDesigner.auth_service.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Busca pelo username (geralmente case-sensitive por padrão)
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);

    // Query explícita para buscar por email de forma case-insensitive
    // Isso garante que "email@exemplo.com" e "Email@exemplo.com" sejam tratados da mesma forma.
    @Query("SELECT u FROM User u WHERE lower(u.email) = lower(:email)")
    Optional<User> findByEmail(@Param("email") String email);
}
