package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.ProductDTO;
import dev.danielmesquita.dmcatalog.repositories.ProductRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ProductServiceIntegrationTest {

  @Autowired private ProductService productService;

  @Autowired private ProductRepository productRepository;

  private Long existingId;
  private Long nonExistingId;
  private Long countTotalProducts;

  @BeforeEach
  public void setUp() {
    existingId = 1L; // Assume this ID exists in the test database
    nonExistingId = 1000L; // Assume this ID does not exist
    countTotalProducts = 25L; // Assume there are 25 products in total
  }

  @Test
  public void deleteShouldDeleteObjectWhenIdExists() {
    productService.delete(existingId);

    boolean exists = productRepository.existsById(existingId);
    Assertions.assertFalse(exists);
  }

  @Test
  public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> productService.delete(nonExistingId));
  }

  @Test
  public void findAllPagedShouldReturnPage() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<ProductDTO> result = productService.findAllPaged(pageable);
    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(countTotalProducts, result.getTotalElements());
  }

  @Test
  public void findAllPagedShouldReturnEmptyPage() {
    PageRequest pageable = PageRequest.of(50, 10);
    Page<ProductDTO> result = productService.findAllPaged(pageable);
    Assertions.assertTrue(result.isEmpty());
  }
}
