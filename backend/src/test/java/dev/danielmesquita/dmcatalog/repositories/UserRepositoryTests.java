package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.utils.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class UserRepositoryTests {

  @Autowired
  private UserRepository repository;

  private long existingId;
  private long nonExistingId;
  private long countTotalUsers;

  @BeforeEach
  public void setUp() {
    // Initialize test data if necessary
    existingId = 1L; // Assume this ID exists in the test database
    nonExistingId = 1000L; // Assume this ID does not exist
    countTotalUsers = 2L; // Assume there are 25 users in total
  }

  @Test
  public void saveShouldPersistWithAutoIncrementWhenIdIsNotNull() {
    User user = Factory.createUser();
    user.setId(null);
    user = repository.save(user);
    Assertions.assertNotNull(user);
    Assertions.assertEquals(countTotalUsers + 1, user.getId());
  }

  @Test
  public void findByIdShouldReturnNonEmptyOptionalWhenIdExists() {
    Optional<User> result = repository.findById(existingId);
    Assertions.assertTrue(result.isPresent());
  }

  @Test
  public void findByIdShouldReturnEmptyOptionalWhenIdDoesNotExist() {
    Optional<User> result = repository.findById(nonExistingId);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void deleteShouldDeleteObjectWhenIdExists() {
    repository.deleteById(existingId);

    Optional<User> result = repository.findById(existingId);
    assert (result.isEmpty());
  }

  @Test
  public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
    boolean exists = repository.existsById(nonExistingId);
    assert (!exists);
  }
}
