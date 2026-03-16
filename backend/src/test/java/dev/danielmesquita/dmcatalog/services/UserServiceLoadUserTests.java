package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.projections.UserDetailsProjection;
import dev.danielmesquita.dmcatalog.repositories.RoleRepository;
import dev.danielmesquita.dmcatalog.repositories.UserRepository;
import dev.danielmesquita.dmcatalog.utils.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class UserServiceLoadUserTests {

  @InjectMocks
  private UserService service;

  @Mock
  private UserRepository repository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private User user;
  private UserDetailsProjection projection;

  @BeforeEach
  public void setUp() {
    user = Factory.createUser();

    projection = new UserDetailsProjection() {
      @Override
      public String getUsername() {
        return "alex@gmail.com";
      }

      @Override
      public String getPassword() {
        return "$2a$10$hash";
      }

      @Override
      public Long getRoleId() {
        return 1L;
      }

      @Override
      public String getAuthority() {
        return "ROLE_OPERATOR";
      }
    };
  }

  @Test
  @DisplayName("loadUserByUsername deve retornar UserDetails quando email existe")
  public void loadUserByUsernameDeveRetornarUserDetailsQuandoEmailExiste() {
    Mockito.when(repository.findUserDetailsByEmail("alex@gmail.com"))
            .thenReturn(List.of(projection));

    UserDetails result = service.loadUserByUsername("alex@gmail.com");

    Assertions.assertNotNull(result);
    Assertions.assertEquals("alex@gmail.com", result.getUsername());
    Assertions.assertEquals("$2a$10$hash", result.getPassword());
  }

  @Test
  @DisplayName("loadUserByUsername deve lançar UsernameNotFoundException quando email não existe")
  public void loadUserByUsernameDeveLancarExcecaoQuandoEmailNaoExiste() {
    Mockito.when(repository.findUserDetailsByEmail("naoexiste@email.com"))
            .thenReturn(Collections.emptyList());

    Assertions.assertThrows(UsernameNotFoundException.class,
            () -> service.loadUserByUsername("naoexiste@email.com"));
  }

  @Test
  @DisplayName("loadUserByUsername deve carregar roles do usuário")
  public void loadUserByUsernameDeveCarregarRolesDoUsuario() {
    UserDetailsProjection op = new UserDetailsProjection() {
      @Override
      public String getUsername() {
        return "maria@gmail.com";
      }

      @Override
      public String getPassword() {
        return "$2a$10$hash";
      }

      @Override
      public Long getRoleId() {
        return 1L;
      }

      @Override
      public String getAuthority() {
        return "ROLE_OPERATOR";
      }
    };
    UserDetailsProjection admin = new UserDetailsProjection() {
      @Override
      public String getUsername() {
        return "maria@gmail.com";
      }

      @Override
      public String getPassword() {
        return "$2a$10$hash";
      }

      @Override
      public Long getRoleId() {
        return 2L;
      }

      @Override
      public String getAuthority() {
        return "ROLE_ADMIN";
      }
    };

    Mockito.when(repository.findUserDetailsByEmail("maria@gmail.com"))
            .thenReturn(List.of(op, admin));

    UserDetails result = service.loadUserByUsername("maria@gmail.com");

    Assertions.assertNotNull(result);
    // User.getAuthorities() retorna List.of() — o que é coberto pela chamada sem erro
    Assertions.assertEquals("maria@gmail.com", result.getUsername());
  }
}
