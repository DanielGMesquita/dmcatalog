package dev.danielmesquita.dmcatalog.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danielmesquita.dmcatalog.dto.UserDTO;
import dev.danielmesquita.dmcatalog.dto.UserInsertDTO;
import dev.danielmesquita.dmcatalog.dto.UserUpdateDTO;
import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.repositories.UserRepository;
import dev.danielmesquita.dmcatalog.services.UserService;
import dev.danielmesquita.dmcatalog.services.exceptions.DatabaseException;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import dev.danielmesquita.dmcatalog.utils.Factory;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Test class for the UserController, using MockMvc to simulate HTTP requests
 * and Mockito to mock the service. WebMvcTest loads the context only for the web layer,
 * without loading the service and repository. AutoConfigureMockMvc configures MockMvc for tests.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests {

  /**
   * MockMvc is a Spring class that allows simulating HTTP requests in integration tests
   * for web controllers.
   */
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService service;

  /**
   * UserInsertValidator depends on UserRepository to check if the e-mail already exists.
   * Since @WebMvcTest does not load the repository layer, we need to mock UserRepository
   * so that Spring can inject the validator into the context.
   */
  @MockitoBean
  private UserRepository userRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private UserDTO userDTO;

  private UserInsertDTO userInsertDTO;

  private UserUpdateDTO userUpdateDTO;

  private PageImpl<UserDTO> page;

  private final Long existingId = 1L;
  private final Long nonExistingId = 1000L;

  @BeforeEach
  public void setUp() {
    User user = Factory.createUser();
    userDTO = new UserDTO(user);
    page = new PageImpl<>(List.of(userDTO));
    userInsertDTO = new UserInsertDTO(user);
    userUpdateDTO = new UserUpdateDTO(user);
  }

  @Test
  public void insertShouldReturnUserDTOCreated() throws Exception {
    Mockito.when(service.insert(userInsertDTO)).thenReturn(userDTO);
    String jsonBody = objectMapper.writeValueAsString(userInsertDTO);

    ResultActions resultActions =
            mockMvc
                    .perform(
                            post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists());

    resultActions.andExpect(jsonPath("$.id").value(userDTO.getId()));
    resultActions.andExpect(jsonPath("$.firstName").value(userDTO.getFirstName()));
  }

  @Test
  public void findAllShouldReturnPage() throws Exception {
    Mockito.when(service.findAllPaged(ArgumentMatchers.any())).thenReturn(page);
    ResultActions resultActions =
            mockMvc
                    .perform(get("/users").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").exists());

    resultActions.andExpect(jsonPath("$.content[0].id").value(userDTO.getId()));
    resultActions.andExpect(jsonPath("$.content[0].firstName").value(userDTO.getFirstName()));
  }

  @Test
  public void findByIdShouldReturnUserWhenIdExists() throws Exception {
    Mockito.when(service.findById(existingId)).thenReturn(userDTO);
    ResultActions resultActions =
            mockMvc
                    .perform(get("/users/{id}", existingId).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists());

    resultActions.andExpect(jsonPath("$.id").value(userDTO.getId()));
    resultActions.andExpect(jsonPath("$.firstName").value(userDTO.getFirstName()));
  }

  @Test
  public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
    Mockito.when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
    mockMvc
            .perform(get("/users/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  public void updateShouldReturnUserWhenIdExists() throws Exception {
    Mockito.when(service.update(ArgumentMatchers.eq(existingId), ArgumentMatchers.any()))
            .thenReturn(userUpdateDTO);

    String jsonBody = objectMapper.writeValueAsString(userUpdateDTO);

    ResultActions resultActions =
            mockMvc
                    .perform(
                            put("/users/{id}", existingId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists());

    resultActions.andExpect(jsonPath("$.id").value(userUpdateDTO.getId()));
    resultActions.andExpect(jsonPath("$.firstName").value(userUpdateDTO.getFirstName()));
  }

  @Test
  public void updateShouldThrowExceptionWhenIdDoesNotExists() throws Exception {
    Mockito.when(service.update(ArgumentMatchers.eq(nonExistingId), ArgumentMatchers.any()))
            .thenThrow(ResourceNotFoundException.class);

    String jsonBody = objectMapper.writeValueAsString(userDTO);

    mockMvc
            .perform(
                    put("/users/{id}", nonExistingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  public void deleteShouldReturnNoContentWhenIdExists() throws Exception {
    Mockito.doNothing().when(service).delete(existingId);

    mockMvc
            .perform(delete("/users/{id}", existingId).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
  }

  @Test
  public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
    Mockito.doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);

    mockMvc
            .perform(delete("/users/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  public void deleteShouldReturnBadRequestWhenDependentId() throws Exception {
    Long dependentId = 4L;
    Mockito.doThrow(DatabaseException.class).when(service).delete(dependentId);

    mockMvc
            .perform(delete("/users/{id}", dependentId).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void insertShouldThrowMethodArgumentNotValidExceptionWhenInvalidData() throws Exception {
    UserInsertDTO invalidUserInsertDTO = userInsertDTO;
    invalidUserInsertDTO.setFirstName("s");
    String jsonBody = objectMapper.writeValueAsString(invalidUserInsertDTO);

    mockMvc
            .perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void updateShouldThrowMethodArgumentNotValidExceptionWhenInvalidData() throws Exception {
    UserDTO invalidUserDTO = userDTO;
    invalidUserDTO.setFirstName("s");
    String jsonBody = objectMapper.writeValueAsString(invalidUserDTO);

    mockMvc
            .perform(
                    put("/users/{id}", existingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void insertShouldThrowUnprocessableEntityWhenEmailAlreadyExists() throws Exception {
    User existingUser = Factory.createUser();
    Mockito.when(userRepository.findByEmail(userInsertDTO.getEmail())).thenReturn(existingUser);

    String jsonBody = objectMapper.writeValueAsString(userInsertDTO);

    mockMvc
            .perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void updateShouldThrowUnprocessableEntityWhenEmailAlreadyExists() throws Exception {
    User existingUser = Factory.createUser();
    Mockito.when(userRepository.findByEmail(userUpdateDTO.getEmail())).thenReturn(existingUser);

    String jsonBody = objectMapper.writeValueAsString(userInsertDTO);

    mockMvc
            .perform(
                    put("/users/{id}", nonExistingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
  }
}
