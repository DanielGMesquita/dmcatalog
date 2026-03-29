package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query(value = "SELECT obj FROM Product obj JOIN FETCH obj.categories",
          countQuery = "SELECT COUNT(obj) FROM Product obj JOIN obj.categories")
  Page<Product> findAllWithCategory(Pageable pageable);
}
