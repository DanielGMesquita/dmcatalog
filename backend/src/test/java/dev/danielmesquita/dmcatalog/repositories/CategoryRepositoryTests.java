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
    countTotalCategories = 3L; // import.sql insere 3 categorias
  }

  @Test
  @DisplayName("save deve persistir com auto-increment quando id é nulo")
  public void saveDevePersistirComAutoIncrementQuandoIdENulo() {
    Category category = new Category();
    category.setName("Nova Categoria");

    category = repository.save(category);

    Assertions.assertNotNull(category.getId());
    Assertions.assertEquals(countTotalCategories + 1, category.getId());
  }

  @Test
  @DisplayName("findById deve retornar Optional não vazio quando id existe")
  public void findByIdDeveRetornarOptionalNaoVazioQuandoIdExiste() {
    Optional<Category> result = repository.findById(existingId);
    Assertions.assertTrue(result.isPresent());
  }

  @Test
  @DisplayName("findById deve retornar Optional vazio quando id não existe")
  public void findByIdDeveRetornarOptionalVazioQuandoIdNaoExiste() {
    Optional<Category> result = repository.findById(nonExistingId);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("findAll paginado deve retornar todas as categorias")
  public void findAllDeveRetornarTodasAsCategorias() {
    Page<Category> result = repository.findAll(PageRequest.of(0, 10));
    Assertions.assertEquals(countTotalCategories, result.getTotalElements());
  }

  @Test
  @DisplayName("delete deve remover objeto quando id existe")
  public void deleteDeveRemoverObjetoQuandoIdExiste() {
    repository.deleteById(existingId);
    Optional<Category> result = repository.findById(existingId);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("existsById deve retornar false quando id não existe")
  public void existsByIdDeveRetornarFalseQuandoIdNaoExiste() {
    Assertions.assertFalse(repository.existsById(nonExistingId));
  }

  @Test
  @DisplayName("existsById deve retornar true quando id existe")
  public void existsByIdDeveRetornarTrueQuandoIdExiste() {
    Assertions.assertTrue(repository.existsById(existingId));
  }
}
