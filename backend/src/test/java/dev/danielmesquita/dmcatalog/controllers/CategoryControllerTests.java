package dev.danielmesquita.dmcatalog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danielmesquita.dmcatalog.dto.CategoryDTO;
import dev.danielmesquita.dmcatalog.services.CategoryService;
import dev.danielmesquita.dmcatalog.services.exceptions.DatabaseException;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CategoryService service;

  @Autowired
  private ObjectMapper objectMapper;

  private CategoryDTO categoryDTO;
  private PageImpl<CategoryDTO> page;

  private final Long existingId = 1L;
  private final Long nonExistingId = 1000L;
  private final Long dependentId = 4L;

  @BeforeEach
  public void setUp() {
    categoryDTO = new CategoryDTO(1L, "Livros");
    page = new PageImpl<>(List.of(categoryDTO));
  }

  // ── GET /categories ───────────────────────────────────────────────────────

  @Test
  @DisplayName("findAll deve retornar página com categorias")
  public void findAllDeveRetornarPaginaComCategorias() throws Exception {
    Mockito.when(service.findAllPaged(ArgumentMatchers.any())).thenReturn(page);

    ResultActions result = mockMvc.perform(get("/categories")
            .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.content").exists());
    result.andExpect(jsonPath("$.content[0].id").value(categoryDTO.getId()));
    result.andExpect(jsonPath("$.content[0].name").value(categoryDTO.getName()));
  }

  // ── GET /categories/{id} ──────────────────────────────────────────────────

  @Test
  @DisplayName("findById deve retornar CategoryDTO quando id existe")
  public void findByIdDeveRetornarCategoryDTOQuandoIdExiste() throws Exception {
    Mockito.when(service.findById(existingId)).thenReturn(categoryDTO);

    ResultActions result = mockMvc.perform(get("/categories/{id}", existingId)
            .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.id").value(categoryDTO.getId()));
    result.andExpect(jsonPath("$.name").value(categoryDTO.getName()));
  }

  @Test
  @DisplayName("findById deve retornar 404 quando id não existe")
  public void findByIdDeveRetornar404QuandoIdNaoExiste() throws Exception {
    Mockito.when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

    mockMvc.perform(get("/categories/{id}", nonExistingId)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  // ── POST /categories ──────────────────────────────────────────────────────

  @Test
  @DisplayName("insert deve retornar 201 e CategoryDTO com Location header quando dados válidos")
  public void insertDeveRetornar201ELocationHeaderQuandoDadosValidos() throws Exception {
    Mockito.when(service.insert(ArgumentMatchers.any())).thenReturn(categoryDTO);
    String jsonBody = objectMapper.writeValueAsString(categoryDTO);

    ResultActions result = mockMvc.perform(post("/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody)
            .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isCreated());
    result.andExpect(header().exists("Location"));
    result.andExpect(jsonPath("$.id").value(categoryDTO.getId()));
    result.andExpect(jsonPath("$.name").value(categoryDTO.getName()));
  }

  // ── PUT /categories/{id} ──────────────────────────────────────────────────

  @Test
  @DisplayName("update deve retornar CategoryDTO quando id existe")
  public void updateDeveRetornarCategoryDTOQuandoIdExiste() throws Exception {
    Mockito.when(service.update(ArgumentMatchers.eq(existingId), ArgumentMatchers.any()))
            .thenReturn(categoryDTO);
    String jsonBody = objectMapper.writeValueAsString(categoryDTO);

    ResultActions result = mockMvc.perform(put("/categories/{id}", existingId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody)
            .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.id").value(categoryDTO.getId()));
    result.andExpect(jsonPath("$.name").value(categoryDTO.getName()));
  }

  @Test
  @DisplayName("update deve retornar 404 quando id não existe")
  public void updateDeveRetornar404QuandoIdNaoExiste() throws Exception {
    Mockito.when(service.update(ArgumentMatchers.eq(nonExistingId), ArgumentMatchers.any()))
            .thenThrow(ResourceNotFoundException.class);
    String jsonBody = objectMapper.writeValueAsString(categoryDTO);

    mockMvc.perform(put("/categories/{id}", nonExistingId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  // ── DELETE /categories/{id} ───────────────────────────────────────────────

  @Test
  @DisplayName("delete deve retornar 204 quando id existe")
  public void deleteDeveRetornar204QuandoIdExiste() throws Exception {
    Mockito.doNothing().when(service).delete(existingId);

    mockMvc.perform(delete("/categories/{id}", existingId)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("delete deve retornar 404 quando id não existe")
  public void deleteDeveRetornar404QuandoIdNaoExiste() throws Exception {
    Mockito.doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);

    mockMvc.perform(delete("/categories/{id}", nonExistingId)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("delete deve retornar 400 quando id tem dependentes")
  public void deleteDeveRetornar400QuandoIdTemDependentes() throws Exception {
    Mockito.doThrow(DatabaseException.class).when(service).delete(dependentId);

    mockMvc.perform(delete("/categories/{id}", dependentId)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }
}
