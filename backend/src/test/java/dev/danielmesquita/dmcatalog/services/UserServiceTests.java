package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.UserDTO;
import dev.danielmesquita.dmcatalog.dto.UserInsertDTO;
import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.repositories.RoleRepository;
import dev.danielmesquita.dmcatalog.repositories.UserRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.DatabaseException;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import dev.danielmesquita.dmcatalog.utils.Factory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

  @InjectMocks
  private UserService service;

  @Mock
  private UserRepository repository;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @Mock
  private RoleRepository roleRepository;

  private long existingId;

  private long dependentId;

  private long nonExistingId;

  private User user = new User();

  private UserDTO userDTO = new UserDTO();

  private UserInsertDTO userInsertDTO = new UserInsertDTO();

  @BeforeEach
  public void setUp() {
    existingId = 1L;
    dependentId = 2L;
    nonExistingId = 1000L;
    user = Factory.createUser();
    userDTO = Factory.createUserDTO();
    userInsertDTO = Factory.createUserInsertDTO();
  }

  @Test
  public void findAllPagedShouldReturnPage() {
    PageImpl<User> page = new PageImpl<>(List.of(user));
    Mockito.when(repository.findAll((Pageable) Mockito.any())).thenReturn(page);

    Assertions.assertDoesNotThrow(
            () -> {
              service.findAllPaged(PageRequest.of(0, 10));
            });
    Mockito.verify(repository).findAll((Pageable) Mockito.any());
  }

  @Test
  public void saveShouldReturnUserDTO() {
    Mockito.when(repository.save(Mockito.any())).thenReturn(user);

    Assertions.assertDoesNotThrow(
            () -> {
              service.insert(userInsertDTO);
            });
    Mockito.verify(repository).save(Mockito.any());
  }

  @Test
  public void deleteShouldDoNothingWhenIdExists() {
    Mockito.when(repository.existsById(existingId)).thenReturn(true);
    Assertions.assertDoesNotThrow(
            () -> {
              service.delete(existingId);
            });
    Mockito.verify(repository).existsById(existingId);
  }

  @Test
  public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
    Mockito.when(repository.existsById(existingId)).thenReturn(false);
    Assertions.assertThrows(
            ResourceNotFoundException.class,
            () -> {
              service.delete(existingId);
            });
    Mockito.verify(repository).existsById(existingId);
  }

  @Test
  public void deleteShouldThrowDatabaseExceptionWhenDataIntegrityViolationOccurs() {
    Mockito.when(repository.existsById(dependentId)).thenReturn(true);
    Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
    Assertions.assertThrows(
            DatabaseException.class,
            () -> {
              service.delete(dependentId);
            });
    Mockito.verify(repository).existsById(dependentId);
    Mockito.verify(repository).deleteById(dependentId);
  }

  @Test
  public void deleteShouldThrowDatabaseExceptionWhenIntegrityViolationOccurs() {
    Mockito.when(repository.existsById(dependentId)).thenReturn(true);
    Mockito.doThrow(DatabaseException.class).when(repository).deleteById(dependentId);
    Assertions.assertThrows(
            DatabaseException.class,
            () -> {
              service.delete(dependentId);
            });
    Mockito.verify(repository).existsById(dependentId);
    Mockito.verify(repository).deleteById(dependentId);
  }

  @Test
  public void findByIdShouldReturnUserDTOWhenIdExists() {
    Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(user));
    Assertions.assertDoesNotThrow(
            () -> {
              service.findById(existingId);
            });
    Mockito.verify(repository).findById(existingId);
  }

  @Test
  public void findByIdShouldReturnEmptyWhenIdDoesNotExists() {
    Assertions.assertThrows(
            ResourceNotFoundException.class,
            () -> {
              service.findById(existingId);
            });
    Mockito.verify(repository).findById(existingId);
  }

  @Test
  public void updateShouldReturnUserDTOWhenIdExists() {
    Mockito.when(repository.getReferenceById(existingId)).thenReturn(user);
    Mockito.when(repository.save(Mockito.any())).thenReturn(user);
    Mockito.when(roleRepository.getReferenceById(1L)).thenReturn(Factory.createRoleUser());
    Mockito.when(roleRepository.getReferenceById(2L)).thenReturn(Factory.createRoleAdmin());
    Assertions.assertDoesNotThrow(
            () -> {
              service.update(existingId, userDTO);
            });
    Mockito.verify(repository).getReferenceById(existingId);
  }

  @Test
  public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
    Mockito.when(repository.getReferenceById(nonExistingId))
            .thenThrow(new EntityNotFoundException("Entity not found"));
    Assertions.assertThrows(
            ResourceNotFoundException.class,
            () -> {
              service.update(nonExistingId, userDTO);
            });
    Mockito.verify(repository).getReferenceById(nonExistingId);
  }
}
