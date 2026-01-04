package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.repositories.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

  @InjectMocks private ProductService service;

  @Mock private ProductRepository repository;

  private long existingId;

  @BeforeEach
  public void setUp() throws Exception {
    existingId = 1L;
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
}
