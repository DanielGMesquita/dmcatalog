package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

@DataJpaTest
public class CategoryRepositoryTests {

  @Autowired
  private CategoryRepository repository;

  private long existingId;
  private long nonExistingId;
  private long countTotalCategories;

  @BeforeEach
  public void setUp() {
    existingId = 1L;
    nonExistingId = 1000L;
    countTotalCategories = 3L; // import.sql inserts 3 categories
  }

  @Test
  @DisplayName("save should persist with auto-increment when id is null")
  public void saveShouldPersistWithAutoIncrementWhenIdIsNull() {
    Category category = new Category();
    category.setName("Nova Categoria");

    category = repository.save(category);

    Assertions.assertNotNull(category.getId());
    Assertions.assertEquals(countTotalCategories + 1, category.getId());
  }

  @Test
  @DisplayName("findById should return a non-empty Optional when id exists")
  public void findByIdShouldReturnNonEmptyOptionalWhenIdExists() {
    Optional<Category> result = repository.findById(existingId);
    Assertions.assertTrue(result.isPresent());
  }

  @Test
  @DisplayName("findById should return an empty Optional when id does not exist")
  public void findByIdShouldReturnEmptyOptionalWhenIdDoesNotExist() {
    Optional<Category> result = repository.findById(nonExistingId);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("findAll paginated should return all categories")
  public void findAllShouldReturnAllCategories() {
    Page<Category> result = repository.findAll(PageRequest.of(0, 10));
    Assertions.assertEquals(countTotalCategories, result.getTotalElements());
  }

  @Test
  @DisplayName("delete should remove the object when id exists")
  public void deleteShouldRemoveObjectWhenIdExists() {
    repository.deleteById(existingId);
    Optional<Category> result = repository.findById(existingId);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("existsById should return false when id does not exist")
  public void existsByIdShouldReturnFalseWhenIdDoesNotExist() {
    Assertions.assertFalse(repository.existsById(nonExistingId));
  }

  @Test
  @DisplayName("existsById should return true when id exists")
  public void existsByIdShouldReturnTrueWhenIdExists() {
    Assertions.assertTrue(repository.existsById(existingId));
  }
}
