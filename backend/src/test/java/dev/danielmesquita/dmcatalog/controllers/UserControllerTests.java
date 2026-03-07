package dev.danielmesquita.dmcatalog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danielmesquita.dmcatalog.dto.UserDTO;
import dev.danielmesquita.dmcatalog.dto.UserInsertDTO;
import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.repositories.UserRepository;
import dev.danielmesquita.dmcatalog.services.UserService;
import dev.danielmesquita.dmcatalog.services.exceptions.DatabaseException;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import dev.danielmesquita.dmcatalog.utils.Factory;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Classe de teste para o controlador UserController, utilizando MockMvc para simular requisições
 * HTTP e Mockito para mockar o serviço. WebMvcTest carrega o contexto somente para a camada web,
 * sem carregar o service e repository. AutoConfigureMockMvc configura o MockMvc para os testes.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests {

  /**
   * MockMvc é uma classe do Spring que permite simular requisições HTTP em testes de integração
   * para controladores web.
   */
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService service;

  /**
   * O UserInsertValidator depende do UserRepository para verificar se o e-mail já existe.
   * Como @WebMvcTest não carrega a camada de repositório, precisamos mockar o UserRepository
   * para que o Spring consiga injetar o validador no contexto.
   */
  @MockitoBean
  private UserRepository userRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private UserDTO userDTO = new UserDTO();

  private UserInsertDTO userInsertDTO = new UserInsertDTO();

  private PageImpl<UserDTO> page;

  private final Long existingId = 1L;
  private final Long nonExistingId = 1000L;

  @BeforeEach
  public void setUp() {
    userDTO = Factory.createUserDTO();
    page = new PageImpl<>(List.of(userDTO));
    userDTO = Factory.createUserDTO();
    userInsertDTO = Factory.createUserInsertDTO();
  }

  @Test
  public void insertShouldReturnUserDTOCreated() throws Exception {
    Mockito.when(service.insert(userInsertDTO)).thenReturn(userDTO);
    String jsonBody = objectMapper.writeValueAsString(userDTO);

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
            .thenReturn(userDTO);

    String jsonBody = objectMapper.writeValueAsString(userDTO);

    ResultActions resultActions =
            mockMvc
                    .perform(
                            put("/users/{id}", existingId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists());

    resultActions.andExpect(jsonPath("$.id").value(userDTO.getId()));
    resultActions.andExpect(jsonPath("$.firstName").value(userDTO.getFirstName()));
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
}
