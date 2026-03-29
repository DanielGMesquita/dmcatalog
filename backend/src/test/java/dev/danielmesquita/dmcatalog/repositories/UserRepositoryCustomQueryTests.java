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
    existingEmail = "alex@gmail.com";      // inserted in import.sql
    nonExistingEmail = "naoexiste@test.com";
  }

  // ── findByEmail ───────────────────────────────────────────────────────────

  @Test
  @DisplayName("findByEmail should return User when email exists")
  public void findByEmailShouldReturnUserWhenEmailExists() {
    User result = repository.findByEmail(existingEmail);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(existingEmail, result.getEmail());
  }

  @Test
  @DisplayName("findByEmail should return null when email does not exist")
  public void findByEmailShouldReturnNullWhenEmailDoesNotExist() {
    User result = repository.findByEmail(nonExistingEmail);
    Assertions.assertNull(result);
  }

  @Test
  @DisplayName("findByEmail should return the correct user among multiple users")
  public void findByEmailShouldReturnCorrectUserAmongMultiple() {
    User result = repository.findByEmail("maria@gmail.com");

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Maria", result.getFirstName());
  }

  // ── findUserDetailsByEmail ────────────────────────────────────────────────

  @Test
  @DisplayName("findUserDetailsByEmail should return a projection with data when email exists")
  public void findUserDetailsByEmailShouldReturnProjectionWhenEmailExists() {
    List<UserDetailsProjection> result = repository.findUserDetailsByEmail(existingEmail);

    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(existingEmail, result.get(0).getUsername());
    Assertions.assertNotNull(result.get(0).getPassword());
    Assertions.assertNotNull(result.get(0).getRoleId());
    Assertions.assertNotNull(result.get(0).getAuthority());
  }

  @Test
  @DisplayName("findUserDetailsByEmail should return an empty list when email does not exist")
  public void findUserDetailsByEmailShouldReturnEmptyListWhenEmailDoesNotExist() {
    List<UserDetailsProjection> result = repository.findUserDetailsByEmail(nonExistingEmail);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("findUserDetailsByEmail should return multiple roles when user has more than one")
  public void findUserDetailsByEmailShouldReturnMultipleRolesWhenUserHasMoreThanOne() {
    // maria@gmail.com has ROLE_OPERATOR and ROLE_ADMIN in import.sql
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
  @DisplayName("findById should return a non-empty Optional when id exists")
  public void findByIdShouldReturnNonEmptyOptionalWhenIdExists() {
    Optional<User> result = repository.findById(1L);
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(existingEmail, result.get().getEmail());
  }
}
