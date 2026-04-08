package dev.danielmesquita.dmcatalog.controllers;

import dev.danielmesquita.dmcatalog.dto.EmailDTO;
import dev.danielmesquita.dmcatalog.dto.NewPasswordDTO;
import dev.danielmesquita.dmcatalog.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  @PutMapping("/new-password")
  public ResponseEntity<Void> saveNewPassword(@Valid @RequestBody NewPasswordDTO body) {
    service.saveNewPassword(body);
    return ResponseEntity.noContent().build();
  }
}
