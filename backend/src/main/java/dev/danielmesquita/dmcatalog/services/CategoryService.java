package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.CategoryDTO;
import dev.danielmesquita.dmcatalog.entities.Category;
import dev.danielmesquita.dmcatalog.repositories.CategoryRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.DatabaseException;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
  private final CategoryRepository repository;

  public CategoryService(CategoryRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Page<CategoryDTO> findAllPaged(Pageable pageable) {
    Page<Category> list = repository.findAll(pageable);
    return list.map(CategoryDTO::new);
  }

  @Transactional(readOnly = true)
  public List<CategoryDTO> findAll() {
    List<Category> list = repository.findAll();
    return list.stream().map(CategoryDTO::new).toList();
  }

  @Transactional(readOnly = true)
  public CategoryDTO findById(Long id) {
    Optional<Category> obj = repository.findById(id);
    Category entity = obj.orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    return new CategoryDTO(entity);
  }

  @Transactional
  public CategoryDTO insert(CategoryDTO dto) {
    Category entity = new Category();
    entity.setName(dto.getName());
    entity = repository.save(entity);
    return new CategoryDTO(entity);
  }

  @Transactional
  public CategoryDTO update(Long id, CategoryDTO dto) {
    try {
      Category entity = repository.getReferenceById(id);
      entity.setName(dto.getName());
      entity = repository.save(entity);
      return new CategoryDTO(entity);
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
}
