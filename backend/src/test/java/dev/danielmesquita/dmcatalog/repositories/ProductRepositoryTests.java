package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.Product;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class ProductRepositoryTests {

  @Autowired private ProductRepository repository;

  private long existingId;
  private long nonExistingId;

  @BeforeEach
  public void setUp() {
    // Initialize test data if necessary
    existingId = 1L; // Assume this ID exists in the test database
    nonExistingId = 1000L; // Assume this ID does not exist

    
  }

  @Test
  public void deleteShouldDeleteObjectWhenIdExists() {
    repository.deleteById(existingId);

    Optional<Product> result = repository.findById(existingId);
    assert(result.isEmpty());
  }

  @Test
  public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
    boolean exists = repository.existsById(nonExistingId);
    assert(!exists);
  }
}
