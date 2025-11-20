package dev.danielmesquita.dmcatalog.controllers;

import dev.danielmesquita.dmcatalog.services.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {
  private final CategoryService service;

  public CategoryController(CategoryService service) {
    this.service = service;
  }

  @GetMapping("/")
  public ResponseEntity<?> findAll() {
    return ResponseEntity.ok(service.findAll());
  }
}
