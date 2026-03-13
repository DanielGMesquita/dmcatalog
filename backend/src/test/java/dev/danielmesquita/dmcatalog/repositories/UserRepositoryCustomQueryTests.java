package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.projections.UserDetailsProjection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

@DataJpaTest
public class UserRepositoryCustomQueryTests {

  @Autowired
  private UserRepository repository;

  private String existingEmail;
  private String nonExistingEmail;

  @BeforeEach
  public void setUp() {
    existingEmail = "alex@gmail.com";      // inserido no import.sql
    nonExistingEmail = "naoexiste@test.com";
  }

  // ── findByEmail ───────────────────────────────────────────────────────────

  @Test
  @DisplayName("findByEmail deve retornar User quando email existe")
  public void findByEmailDeveRetornarUserQuandoEmailExiste() {
    User result = repository.findByEmail(existingEmail);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(existingEmail, result.getEmail());
  }

  @Test
  @DisplayName("findByEmail deve retornar null quando email não existe")
  public void findByEmailDeveRetornarNullQuandoEmailNaoExiste() {
    User result = repository.findByEmail(nonExistingEmail);
    Assertions.assertNull(result);
  }

  @Test
  @DisplayName("findByEmail deve retornar o usuário correto entre múltiplos usuários")
  public void findByEmailDeveRetornarUsuarioCorretoEntreMultiplos() {
    User result = repository.findByEmail("maria@gmail.com");

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Maria", result.getFirstName());
  }

  // ── findUserDetailsByEmail ────────────────────────────────────────────────

  @Test
  @DisplayName("findUserDetailsByEmail deve retornar projeção com dados quando email existe")
  public void findUserDetailsByEmailDeveRetornarProjecaoQuandoEmailExiste() {
    List<UserDetailsProjection> result = repository.findUserDetailsByEmail(existingEmail);

    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(existingEmail, result.get(0).getUsername());
    Assertions.assertNotNull(result.get(0).getPassword());
    Assertions.assertNotNull(result.get(0).getRoleId());
    Assertions.assertNotNull(result.get(0).getAuthority());
  }

  @Test
  @DisplayName("findUserDetailsByEmail deve retornar lista vazia quando email não existe")
  public void findUserDetailsByEmailDeveRetornarListaVaziaQuandoEmailNaoExiste() {
    List<UserDetailsProjection> result = repository.findUserDetailsByEmail(nonExistingEmail);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("findUserDetailsByEmail deve retornar múltiplas roles quando usuário tem mais de uma")
  public void findUserDetailsByEmailDeveRetornarMultiplasRolesQuandoUsuarioTemMaisDeUma() {
    // maria@gmail.com tem ROLE_OPERATOR e ROLE_ADMIN no import.sql
    List<UserDetailsProjection> result = repository.findUserDetailsByEmail("maria@gmail.com");

    Assertions.assertEquals(2, result.size());
    boolean hasOperator = result.stream()
            .anyMatch(p -> "ROLE_OPERATOR".equals(p.getAuthority()));
    boolean hasAdmin = result.stream()
            .anyMatch(p -> "ROLE_ADMIN".equals(p.getAuthority()));
    Assertions.assertTrue(hasOperator);
    Assertions.assertTrue(hasAdmin);
  }

  // ── findById ──────────────────────────────────────────────────────────────

  @Test
  @DisplayName("findById deve retornar Optional não vazio quando id existe")
  public void findByIdDeveRetornarOptionalNaoVazioQuandoIdExiste() {
    Optional<User> result = repository.findById(1L);
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(existingEmail, result.get().getEmail());
  }
}
