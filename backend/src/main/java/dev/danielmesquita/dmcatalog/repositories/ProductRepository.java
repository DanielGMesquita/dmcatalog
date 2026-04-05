package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.Product;
import dev.danielmesquita.dmcatalog.projections.ProductProjection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query(
      nativeQuery = true,
      value =
          """
                  SELECT DISTINCT tb_product.id, tb_product.name
                  FROM tb_product
                  INNER JOIN tb_product_category ON tb_product.id = tb_product_category.product_id
                  WHERE (:categoriesIds IS NULL OR tb_product_category.category_id IN (:categoriesIds))
                  AND (LOWER(tb_product.name) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL)
                  ORDER BY tb_product.name
                  """,
      countQuery =
          """
                  SELECT COUNT(*) FROM(
                  SELECT DISTINCT tb_product.id, tb_product.name
                  FROM tb_product
                  INNER JOIN tb_product_category ON tb_product.id = tb_product_category.product_id
                  WHERE (:categoriesIds IS NULL OR tb_product_category.category_id IN (:categoriesIds))
                  AND (LOWER(tb_product.name) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL)
                  ) AS tb_result
                  """)
  Page<ProductProjection> searchAll(List<Long> categoriesIds, String name, Pageable pageable);

  @Query(
      "SELECT p FROM Product p JOIN FETCH p.categories WHERE p.id IN :productIds ORDER BY p.name")
  List<Product> searchProductsWithCategories(List<Long> productIds);
}
