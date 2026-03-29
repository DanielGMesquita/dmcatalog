package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.CategoryDTO;
import dev.danielmesquita.dmcatalog.dto.ProductDTO;
import dev.danielmesquita.dmcatalog.entities.Category;
import dev.danielmesquita.dmcatalog.entities.Product;
import dev.danielmesquita.dmcatalog.repositories.CategoryRepository;
import dev.danielmesquita.dmcatalog.repositories.ProductRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.DatabaseException;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductService {
  private final ProductRepository repository;
  private final CategoryRepository categoryRepository;

  public ProductService(ProductRepository repository, CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Page<ProductDTO> findAllPaged(Pageable pageable) {
    Page<Product> list = repository.findAllWithCategory(pageable);
    return list.map(ProductDTO::new);
  }

  @Transactional(readOnly = true)
  public ProductDTO findById(Long id) {
    Optional<Product> obj = repository.findById(id);
    Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
    return new ProductDTO(entity);
  }

  @Transactional
  public ProductDTO insert(ProductDTO dto) {
    Product entity = new Product();
    copyDtoToEntity(dto, entity);
    entity = repository.save(entity);
    return new ProductDTO(entity);
  }

  @Transactional
  public ProductDTO update(Long id, ProductDTO dto) {
    try {
      Product entity = repository.getReferenceById(id);
      copyDtoToEntity(dto, entity);
      entity = repository.save(entity);
      return new ProductDTO(entity);
    } catch (EntityNotFoundException e) {
      throw new ResourceNotFoundException("Id not found " + id);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new ResourceNotFoundException("Id not found " + id);
    }
    try {
      repository.deleteById(id);
    } catch (DataIntegrityViolationException e) {
      throw new DatabaseException("Integrity violation");
    }
  }

  private void copyDtoToEntity(ProductDTO dto, Product entity) {

    entity.setName(dto.getName());
    entity.setDescription(dto.getDescription());
    entity.setDate(dto.getDate());
    entity.setImgUrl(dto.getImgUrl());
    entity.setPrice(dto.getPrice());

    entity.getCategories().clear();
    for (CategoryDTO catDto : dto.getCategories()) {
      Category category = categoryRepository.getReferenceById(catDto.getId());
      entity.getCategories().add(category);
    }
  }
}
