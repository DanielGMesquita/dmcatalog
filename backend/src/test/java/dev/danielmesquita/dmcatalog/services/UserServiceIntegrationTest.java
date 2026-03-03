package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.UserDTO;
import dev.danielmesquita.dmcatalog.repositories.UserRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  private Long existingId;
  private Long nonExistingId;
  private Long countTotalUsers;

  @BeforeEach
  public void setUp() {
    existingId = 1L; // Assume this ID exists in the test database
    nonExistingId = 1000L; // Assume this ID does not exist
    countTotalUsers = 2L; // Assume there are 25 users in total
  }

  @Test
  public void deleteShouldDeleteObjectWhenIdExists() {
    userService.delete(existingId);

    boolean exists = userRepository.existsById(existingId);
    Assertions.assertFalse(exists);
  }

  @Test
  public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
    Assertions.assertThrows(
            ResourceNotFoundException.class, () -> userService.delete(nonExistingId));
  }

  @Test
  public void findAllPagedShouldReturnPage() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<UserDTO> result = userService.findAllPaged(pageable);
    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(countTotalUsers, result.getTotalElements());
  }

  @Test
  public void findAllPagedShouldReturnEmptyPageWhenPageDoesNotExists() {
    PageRequest pageable = PageRequest.of(50, 10);
    Page<UserDTO> result = userService.findAllPaged(pageable);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void findAllPagedShouldReturnOrderedPageWhenSortByName() {
    PageRequest pageable = PageRequest.of(0, 10, Sort.by("firstName"));
    Page<UserDTO> result = userService.findAllPaged(pageable);
    Assertions.assertFalse(result.isEmpty());
  }
}
