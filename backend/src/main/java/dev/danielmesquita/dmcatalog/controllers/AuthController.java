package dev.danielmesquita.dmcatalog.controllers;

import dev.danielmesquita.dmcatalog.dto.EmailDTO;
import dev.danielmesquita.dmcatalog.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth")
public class AuthController {

  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/recovery-token")
  public ResponseEntity<Void> createRecoveryToken(@Valid @RequestBody EmailDTO body) {
    service.createRecoveryToken(body);
    return ResponseEntity.noContent().build();
  }
}
