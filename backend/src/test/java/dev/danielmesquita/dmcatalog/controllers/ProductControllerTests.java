package dev.danielmesquita.dmcatalog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danielmesquita.dmcatalog.dto.ProductDTO;
import dev.danielmesquita.dmcatalog.services.ProductService;
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
 * Classe de teste para o controlador ProductController, utilizando MockMvc para simular requisições
 * HTTP e Mockito para mockar o serviço. WebMvcTest carrega o contexto somente para a camada web,
 * sem carregar o service e repository. AutoConfigureMockMvc configura o MockMvc para os testes.
 */
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTests {

  /**
   * MockMvc é uma classe do Spring que permite simular requisições HTTP em testes de integração
   * para controladores web.
   */
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ProductService service;

  @Autowired
  private ObjectMapper objectMapper;

  private ProductDTO productDTO;

  private PageImpl<ProductDTO> page;

  private final Long existingId = 1L;
  private final Long nonExistingId = 1000L;

  @BeforeEach
  public void setUp() {
    productDTO = Factory.createProductDTO();
    page = new PageImpl<>(List.of(productDTO));
  }

  @Test
  public void insertShouldReturnProductDTOCreated() throws Exception {
    Mockito.when(service.insert(productDTO)).thenReturn(productDTO);
    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
            mockMvc
                    .perform(
                            post("/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists());

    resultActions.andExpect(jsonPath("$.id").value(productDTO.getId()));
    resultActions.andExpect(jsonPath("$.name").value(productDTO.getName()));
  }

  @Test
  public void findAllShouldReturnPage() throws Exception {
    Mockito.when(service.findAllPaged(ArgumentMatchers.any())).thenReturn(page);
    ResultActions resultActions =
            mockMvc
                    .perform(get("/products").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").exists());

    resultActions.andExpect(jsonPath("$.content[0].id").value(productDTO.getId()));
    resultActions.andExpect(jsonPath("$.content[0].name").value(productDTO.getName()));
  }

  @Test
  public void findByIdShouldReturnProductWhenIdExists() throws Exception {
    Mockito.when(service.findById(existingId)).thenReturn(productDTO);
    ResultActions resultActions =
            mockMvc
                    .perform(get("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists());

    resultActions.andExpect(jsonPath("$.id").value(productDTO.getId()));
    resultActions.andExpect(jsonPath("$.name").value(productDTO.getName()));
  }

  @Test
  public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
    Mockito.when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
    mockMvc
            .perform(get("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  public void updateShouldReturnProductWhenIdExists() throws Exception {
    Mockito.when(service.update(ArgumentMatchers.eq(existingId), ArgumentMatchers.any()))
            .thenReturn(productDTO);

    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
            mockMvc
                    .perform(
                            put("/products/{id}", existingId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists());

    resultActions.andExpect(jsonPath("$.id").value(productDTO.getId()));
    resultActions.andExpect(jsonPath("$.name").value(productDTO.getName()));
  }

  @Test
  public void updateShouldThrowExceptionWhenIdDoesNotExists() throws Exception {
    Mockito.when(service.update(ArgumentMatchers.eq(nonExistingId), ArgumentMatchers.any()))
            .thenThrow(ResourceNotFoundException.class);

    String jsonBody = objectMapper.writeValueAsString(productDTO);

    mockMvc
            .perform(
                    put("/products/{id}", nonExistingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  public void updateShouldThrowMethodArgumentNotValidExceptionWhenInvalidData() throws Exception {
    ProductDTO invalidProductDTO = productDTO;
    invalidProductDTO.setName("s");
    String jsonBody = objectMapper.writeValueAsString(invalidProductDTO);

    mockMvc
            .perform(
                    put("/products/{id}", existingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void deleteShouldReturnNoContentWhenIdExists() throws Exception {
    Mockito.doNothing().when(service).delete(existingId);

    mockMvc
            .perform(delete("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
  }

  @Test
  public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
    Mockito.doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);

    mockMvc
            .perform(delete("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  public void deleteShouldReturnBadRequestWhenDependentId() throws Exception {
    Long dependentId = 4L;
    Mockito.doThrow(DatabaseException.class).when(service).delete(dependentId);

    mockMvc
            .perform(delete("/products/{id}", dependentId).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void insertShouldReturnUnprocessableEntityWhenInvalidData() throws Exception {
    ProductDTO invalidProductDTO = productDTO;
    invalidProductDTO.setName("s");
    String jsonBody = objectMapper.writeValueAsString(invalidProductDTO);

    mockMvc
            .perform(
                    post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void updateShouldReturnUnprocessableEntityWhenInvalidData() throws Exception {
    ProductDTO invalidProductDTO = productDTO;
    invalidProductDTO.setName("s");
    String jsonBody = objectMapper.writeValueAsString(invalidProductDTO);

    mockMvc
            .perform(
                    put("/products/{id}", existingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
  }
}
