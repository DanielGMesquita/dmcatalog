package dev.danielmesquita.dmcatalog.controllers;

import dev.danielmesquita.dmcatalog.dto.UserDTO;
import dev.danielmesquita.dmcatalog.dto.UserInsertDTO;
import dev.danielmesquita.dmcatalog.dto.UserUpdateDTO;
import dev.danielmesquita.dmcatalog.services.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "/users")
public class UserController {
  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<Page<UserDTO>> findAll(Pageable pageable) {
    Page<UserDTO> list = service.findAllPaged(pageable);
    return ResponseEntity.ok().body(list);
  }
  
  @GetMapping(value = "/{id}")
  public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
    UserDTO dto = service.findById(id);
    return ResponseEntity.ok().body(dto);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping
  public ResponseEntity<UserDTO> insert(@Valid @RequestBody UserInsertDTO dto) {
    UserDTO newDto = service.insert(dto);
    URI uri =
            ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(newDto.getId())
                    .toUri();
    return ResponseEntity.created(uri).body(newDto);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping(value = "/{id}")
  public ResponseEntity<UserUpdateDTO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
    dto = service.update(id, dto);
    return ResponseEntity.ok().body(dto);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping(value = "/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
