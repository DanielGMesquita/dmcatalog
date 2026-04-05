package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.CategoryDTO;
import dev.danielmesquita.dmcatalog.dto.ProductDTO;
import dev.danielmesquita.dmcatalog.entities.Category;
import dev.danielmesquita.dmcatalog.entities.Product;
import dev.danielmesquita.dmcatalog.projections.ProductProjection;
import dev.danielmesquita.dmcatalog.repositories.CategoryRepository;
import dev.danielmesquita.dmcatalog.repositories.ProductRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.DatabaseException;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import dev.danielmesquita.dmcatalog.utils.Factory;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

  @InjectMocks private ProductService service;

  @Mock private ProductRepository repository;

  @Mock private CategoryRepository categoryRepository;

  private long existingId;

  private long dependentId;

  private long nonExistingId;

  private Product product = new Product();

  private ProductDTO productDTO = new ProductDTO();

  @BeforeEach
  public void setUp() {
    existingId = 1L;
    dependentId = 2L;
    nonExistingId = 1000L;
    product = Factory.createProduct();
    productDTO = Factory.createProductDTO();
  }

  @Test
  public void findAllPagedShouldReturnPage() {
    Page<ProductProjection> page = new PageImpl<>(List.of(Factory.createProductProjection()));
    Mockito.when(
            repository.searchAll(Mockito.any(), Mockito.anyString(), Mockito.any(Pageable.class)))
        .thenReturn(page);
    Mockito.when(repository.searchProductsWithCategories(Mockito.any()))
        .thenReturn(List.of(product));

    Assertions.assertDoesNotThrow(
        () -> {
          service.findAllPaged("", "0", PageRequest.of(0, 10));
        });
    Mockito.verify(repository)
        .searchAll(Mockito.any(), Mockito.anyString(), Mockito.any(Pageable.class));
    Mockito.verify(repository).searchProductsWithCategories(Mockito.any());
  }

  @Test
  public void saveShouldReturnProductDTO() {
    Mockito.when(repository.save(Mockito.any())).thenReturn(product);

    Assertions.assertDoesNotThrow(
        () -> {
          service.insert(productDTO);
        });
    Mockito.verify(repository).save(Mockito.any());
  }

  @Test
  public void saveShouldReturnProductDTOWithProductWithMoreThanOneCategory() {
    Category category1 = Factory.createCategory(1L, "Electronics");
    Category category2 = Factory.createCategory(2L, "Books");
    product.getCategories().add(category1);
    product.getCategories().add(category2);
    productDTO.getCategories().add(new CategoryDTO(category1));
    productDTO.getCategories().add(new CategoryDTO(category2));

    Mockito.when(repository.save(Mockito.any())).thenReturn(product);
    Assertions.assertDoesNotThrow(
        () -> {
          service.insert(productDTO);
        });
    Mockito.verify(repository).save(Mockito.any());
  }

  @Test
  public void deleteShouldDoNothingWhenIdExists() {
    Mockito.when(repository.existsById(existingId)).thenReturn(true);
    Assertions.assertDoesNotThrow(
        () -> {
          service.delete(existingId);
        });
    Mockito.verify(repository).existsById(existingId);
  }

  @Test
  public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
    Mockito.when(repository.existsById(existingId)).thenReturn(false);
    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> {
          service.delete(existingId);
        });
    Mockito.verify(repository).existsById(existingId);
  }

  @Test
  public void deleteShouldThrowDatabaseExceptionWhenDataIntegrityViolationOccurs() {
    Mockito.when(repository.existsById(dependentId)).thenReturn(true);
    Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
    Assertions.assertThrows(
        DatabaseException.class,
        () -> {
          service.delete(dependentId);
        });
    Mockito.verify(repository).existsById(dependentId);
    Mockito.verify(repository).deleteById(dependentId);
  }

  @Test
  public void deleteShouldThrowDatabaseExceptionWhenIntegrityViolationOccurs() {
    Mockito.when(repository.existsById(dependentId)).thenReturn(true);
    Mockito.doThrow(DatabaseException.class).when(repository).deleteById(dependentId);
    Assertions.assertThrows(
        DatabaseException.class,
        () -> {
          service.delete(dependentId);
        });
    Mockito.verify(repository).existsById(dependentId);
    Mockito.verify(repository).deleteById(dependentId);
  }

  @Test
  public void findByIdShouldReturnProductDTOWhenIdExists() {
    Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
    Assertions.assertDoesNotThrow(
        () -> {
          service.findById(existingId);
        });
    Mockito.verify(repository).findById(existingId);
  }

  @Test
  public void findByIdShouldReturnEmptyWhenIdDoesNotExists() {
    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> {
          service.findById(existingId);
        });
    Mockito.verify(repository).findById(existingId);
  }

  @Test
  public void updateShouldReturnProductDTOWhenIdExists() {
    Mockito.when(repository.getReferenceById(existingId)).thenReturn(product);
    Mockito.when(repository.save(Mockito.any())).thenReturn(product);
    Assertions.assertDoesNotThrow(
        () -> {
          service.update(existingId, productDTO);
        });
    Mockito.verify(repository).getReferenceById(existingId);
  }

  @Test
  public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
    Mockito.when(repository.getReferenceById(nonExistingId))
        .thenThrow(new EntityNotFoundException("Entity not found"));
    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> {
          service.update(nonExistingId, productDTO);
        });
    Mockito.verify(repository).getReferenceById(nonExistingId);
  }
}
