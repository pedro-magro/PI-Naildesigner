package br.com.nailDesigner.auth_service.Services;

import br.com.nailDesigner.auth_service.Models.Role;
import br.com.nailDesigner.auth_service.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.nailDesigner.auth_service.Models.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp(){
        user = new User("pedro", "123", "pedro@gmail.com", "123", Role.USER);
    }

    @Test
    public void deveBuscarUsuarioPorUsername(){
        when(userRepository.findByUsername("pedro")).thenReturn(Optional.of(user));

        UserDetails resultado = userDetailsService.loadUserByUsername("pedro");

        assertNotNull(resultado);
        assertEquals(user.getUsername(), resultado.getUsername());
        verify(userRepository).findByUsername("pedro");
    }

    @Test
    public void deveBuscarUsuarioPorIdQuandoIdentificadorForUuid(){
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDetails resultado = userDetailsService.loadUserByUsername(user.getId().toString());

        assertNotNull(resultado);
        assertEquals(user.getUsername(), resultado.getUsername());
        verify(userRepository).findById(user.getId());

    }

    @Test
    public void deveRemoverEspacosAntesDeBuscarUsuario(){
        when(userRepository.findByUsername("pedro")).thenReturn(Optional.of(user));

        UserDetails resultado = userDetailsService.loadUserByUsername(" pedro ");

        assertNotNull(resultado);
        verify(userRepository).findByUsername("pedro");
    }

    @Test
    public void deveLancarExcecaoQuandoUsuarioNaoEncontrado(){
        when(userRepository.findByUsername("pedro")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> userDetailsService.loadUserByUsername("pedro"));
    }

    @Test
    public void deveLancarExcecaoQuandoEmailNaoEncontrado(){
        when(userRepository.findByEmail("pedro@gmail.com")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> userDetailsService.loadUserByUsername("pedro@gmail.com"));
    }

    @Test
    public void deveLancarExcecaoQuandoIdNaoEncontrado(){
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> userDetailsService.loadUserByUsername(user.getId().toString()));
    }

    @Test
    public void deveRetornarInstanciaDeUser(){
        when(userRepository.findByUsername("pedro")).thenReturn(Optional.of(user));

        UserDetails resultado = userDetailsService.loadUserByUsername("pedro");

        assertInstanceOf(User.class, resultado);
    }

}
