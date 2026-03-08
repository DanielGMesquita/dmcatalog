package dev.danielmesquita.dmcatalog.utils;

import dev.danielmesquita.dmcatalog.dto.ProductDTO;
import dev.danielmesquita.dmcatalog.entities.Category;
import dev.danielmesquita.dmcatalog.entities.Product;
import dev.danielmesquita.dmcatalog.entities.Role;
import dev.danielmesquita.dmcatalog.entities.User;

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

  public static Category createCategory(long id, String name) {
    return new Category(id, name);
  }

  public static Role createRoleUser() {
    return new Role(1L, "ROLE_OPERATOR");
  }

  public static Role createRoleAdmin() {
    return new Role(2L, "ROLE_ADMIN");
  }

  public static User createUser() {
    User user = new User();
    user.setId(1L);
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("mail@mail.com");
    user.setPassword("123456");
    user.getRoles().add(createRoleUser());
    user.getRoles().add(createRoleAdmin());
    return user;
  }
}
