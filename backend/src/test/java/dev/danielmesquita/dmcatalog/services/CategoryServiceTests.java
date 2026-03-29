package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.CategoryDTO;
import dev.danielmesquita.dmcatalog.entities.Category;
import dev.danielmesquita.dmcatalog.repositories.CategoryRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.DatabaseException;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTests {

  @InjectMocks
  private CategoryService service;

  @Mock
  private CategoryRepository repository;

  private long existingId;
  private long nonExistingId;
  private long dependentId;
  private Category category;
  private CategoryDTO categoryDTO;

  @BeforeEach
  public void setUp() {
    existingId = 1L;
    nonExistingId = 1000L;
    dependentId = 2L;
    category = new Category(1L, "Livros");
    categoryDTO = new CategoryDTO(1L, "Livros");
  }

  @Test
  @DisplayName("findAll should return a return a list when called")
  public void findAllShouldReturnAListOfCategories() {
    List<Category> listOfCategories = List.of(category);
    Mockito.when(repository.findAll()).thenReturn(listOfCategories);

    Assertions.assertDoesNotThrow(() -> service.findAll());
    Mockito.verify(repository).findAll();
  }

  // ── findAllPaged ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("findAllPaged should return a page when called")
  public void findAllPagedShouldReturnPageWhenCalled() {
    PageImpl<Category> page = new PageImpl<>(List.of(category));
    Mockito.when(repository.findAll((Pageable) Mockito.any())).thenReturn(page);

    Assertions.assertDoesNotThrow(() -> service.findAllPaged(PageRequest.of(0, 10)));
    Mockito.verify(repository).findAll((Pageable) Mockito.any());
  }

  // ── findById ──────────────────────────────────────────────────────────────

  @Test
  @DisplayName("findById should return CategoryDTO when id exists")
  public void findByIdShouldReturnCategoryDTOWhenIdExists() {
    Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(category));

    CategoryDTO result = service.findById(existingId);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(category.getId(), result.getId());
    Assertions.assertEquals(category.getName(), result.getName());
    Mockito.verify(repository).findById(existingId);
  }

  @Test
  @DisplayName("findById should throw ResourceNotFoundException when id does not exist")
  public void findByIdShouldThrowExceptionWhenIdDoesNotExist() {
    Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

    Assertions.assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistingId));
    Mockito.verify(repository).findById(nonExistingId);
  }

  // ── insert ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("insert should return CategoryDTO when data is valid")
  public void insertShouldReturnCategoryDTOWhenDataIsValid() {
    Mockito.when(repository.save(Mockito.any())).thenReturn(category);

    CategoryDTO result = service.insert(categoryDTO);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(category.getName(), result.getName());
    Mockito.verify(repository).save(Mockito.any());
  }

  // ── update ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("update should return CategoryDTO when id exists")
  public void updateShouldReturnCategoryDTOWhenIdExists() {
    Mockito.when(repository.getReferenceById(existingId)).thenReturn(category);
    Mockito.when(repository.save(Mockito.any())).thenReturn(category);

    CategoryDTO updatedDTO = new CategoryDTO(existingId, "Eletrônicos");
    CategoryDTO result = service.update(existingId, updatedDTO);

    Assertions.assertNotNull(result);
    Mockito.verify(repository).getReferenceById(existingId);
    Mockito.verify(repository).save(Mockito.any());
  }

  @Test
  @DisplayName("update should throw ResourceNotFoundException when id does not exist")
  public void updateShouldThrowExceptionWhenIdDoesNotExist() {
    Mockito.when(repository.getReferenceById(nonExistingId))
            .thenThrow(new EntityNotFoundException("Entity not found"));

    Assertions.assertThrows(
            ResourceNotFoundException.class,
            () -> service.update(nonExistingId, categoryDTO));
    Mockito.verify(repository).getReferenceById(nonExistingId);
  }

  // ── delete ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("delete should not throw an exception when id exists")
  public void deleteShouldNotThrowExceptionWhenIdExists() {
    Mockito.when(repository.existsById(existingId)).thenReturn(true);

    Assertions.assertDoesNotThrow(() -> service.delete(existingId));
    Mockito.verify(repository).existsById(existingId);
    Mockito.verify(repository).deleteById(existingId);
  }

  @Test
  @DisplayName("delete should throw ResourceNotFoundException when id does not exist")
  public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
    Mockito.when(repository.existsById(nonExistingId)).thenReturn(false);

    Assertions.assertThrows(
            ResourceNotFoundException.class, () -> service.delete(nonExistingId));
    Mockito.verify(repository).existsById(nonExistingId);
  }

  @Test
  @DisplayName("delete should throw DatabaseException when there is a referential integrity violation")
  public void deleteShouldThrowDatabaseExceptionWhenIntegrityViolationOccurs() {
    Mockito.when(repository.existsById(dependentId)).thenReturn(true);
    Mockito.doThrow(DataIntegrityViolationException.class)
            .when(repository).deleteById(dependentId);

    Assertions.assertThrows(DatabaseException.class, () -> service.delete(dependentId));
    Mockito.verify(repository).existsById(dependentId);
    Mockito.verify(repository).deleteById(dependentId);
  }
}
