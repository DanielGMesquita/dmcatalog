package dev.danielmesquita.dmcatalog.utils;

import dev.danielmesquita.dmcatalog.dto.ProductDTO;
import dev.danielmesquita.dmcatalog.entities.Product;

public class Factory {
  public static Product createProduct() {
    return new Product(
        1L,
        "Test Product",
        "This is a test product",
        99.99,
        "https://example.com/image.jpg",
        java.time.Instant.now());
  }

  public static ProductDTO createProductDTO() {
    Product product = createProduct();
    return new ProductDTO(product);
  }
}
