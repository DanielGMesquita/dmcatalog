package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.ProductDTO;
import dev.danielmesquita.dmcatalog.entities.Product;
import dev.danielmesquita.dmcatalog.repositories.ProductRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.DatabaseException;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import dev.danielmesquita.dmcatalog.utils.Factory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

  @InjectMocks private ProductService service;

  @Mock private ProductRepository repository;

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
    PageImpl<Product> page = new PageImpl<>(List.of(product));

    Mockito.when(repository.save(Mockito.any())).thenReturn(product);
    Mockito.when(repository.findAll((Pageable) Mockito.any())).thenReturn(page);
    Mockito.when(repository.getReferenceById(existingId)).thenReturn(product);
  }

  @Test
  public void findAllPagedShouldReturnPage() {
    Assertions.assertDoesNotThrow(
        () -> {
          service.findAllPaged(PageRequest.of(0, 10));
        });
    Mockito.verify(repository).findAll((Pageable) Mockito.any());
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
    Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());
    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> {
          service.findById(existingId);
        });
    Mockito.verify(repository).findById(existingId);
  }

  @Test
  public void updateShouldReturnProductDTOWhenIdExists() {
    Assertions.assertDoesNotThrow(
        () -> {
          service.update(existingId, productDTO);
        });
    Mockito.verify(repository).getReferenceById(existingId);
  }
}
