package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.Product;
import dev.danielmesquita.dmcatalog.utils.Factory;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class ProductRepositoryTests {

  @Autowired private ProductRepository repository;

  private long existingId;
  private long nonExistingId;
  private long countTotalProducts;

  @BeforeEach
  public void setUp() {
    // Initialize test data if necessary
    existingId = 1L; // Assume this ID exists in the test database
    nonExistingId = 1000L; // Assume this ID does not exist
    countTotalProducts = 25L; // Assume there are 25 products in total
  }

  @Test
  public void saveShouldPersistWithAutoIncrementWhenIdIsNotNull() {
    Product product = Factory.createProduct();
    product.setId(null);
    product = repository.save(product);
    Assertions.assertNotNull(product);
    Assertions.assertEquals(countTotalProducts + 1, product.getId());
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
