package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.Product;
import dev.danielmesquita.dmcatalog.utils.Factory;
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
  public void saveShouldPersistWithAutoIncrementWhenIdIsNotNull() {
    Product product = repository.save(Factory.createProduct());
    assert (product.getId() != null);
  }

  @Test
  public void saveShouldPersistWithAutoIncrementWhenIdIsNull() {
    Product product = repository.save(Factory.createProduct());
    product.setId(null);

    product = repository.save(product);
    assert (product.getId() == null);
  }

  @Test
  public void deleteShouldDeleteObjectWhenIdExists() {
    repository.deleteById(existingId);

    Optional<Product> result = repository.findById(existingId);
    assert (result.isEmpty());
  }

  @Test
  public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
    boolean exists = repository.existsById(nonExistingId);
    assert (!exists);
  }
}
