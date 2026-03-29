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

  private final Long existingId = 1L;
  private final Long nonExistingId = 1000L;

  @BeforeEach
  public void setUp() {
    categoryDTO = new CategoryDTO(1L, "Livros");
  }

  // ── GET /categories ───────────────────────────────────────────────────────

  @Test
  @DisplayName("findAll should return a list with categories")
  public void findAllShouldReturnListWithCategories() throws Exception {
    Mockito.when(service.findAll()).thenReturn(List.of(categoryDTO));

    ResultActions result = mockMvc.perform(get("/categories")
            .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$[0].id").value(categoryDTO.getId()));
  }

  // ── GET /categories/{id} ──────────────────────────────────────────────────

  @Test
  @DisplayName("findById should return CategoryDTO when id exists")
  public void findByIdShouldReturnCategoryDTOWhenIdExists() throws Exception {
    Mockito.when(service.findById(existingId)).thenReturn(categoryDTO);

    ResultActions result = mockMvc.perform(get("/categories/{id}", existingId)
            .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.id").value(categoryDTO.getId()));
    result.andExpect(jsonPath("$.name").value(categoryDTO.getName()));
  }

  @Test
  @DisplayName("findById should return 404 when id does not exist")
  public void findByIdShouldReturn404WhenIdDoesNotExist() throws Exception {
    Mockito.when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

    mockMvc.perform(get("/categories/{id}", nonExistingId)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  // ── POST /categories ──────────────────────────────────────────────────────

  @Test
  @DisplayName("insert should return 201 and CategoryDTO with Location header when data is valid")
  public void insertShouldReturn201AndLocationHeaderWhenDataIsValid() throws Exception {
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
  @DisplayName("update should return CategoryDTO when id exists")
  public void updateShouldReturnCategoryDTOWhenIdExists() throws Exception {
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
  @DisplayName("update should return 404 when id does not exist")
  public void updateShouldReturn404WhenIdDoesNotExist() throws Exception {
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
  @DisplayName("delete should return 204 when id exists")
  public void deleteShouldReturn204WhenIdExists() throws Exception {
    Mockito.doNothing().when(service).delete(existingId);

    mockMvc.perform(delete("/categories/{id}", existingId)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("delete should return 404 when id does not exist")
  public void deleteShouldReturn404WhenIdDoesNotExist() throws Exception {
    Mockito.doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);

    mockMvc.perform(delete("/categories/{id}", nonExistingId)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("delete should return 400 when id has dependents")
  public void deleteShouldReturn400WhenIdHasDependents() throws Exception {
    Long dependentId = 4L;
    Mockito.doThrow(DatabaseException.class).when(service).delete(dependentId);

    mockMvc.perform(delete("/categories/{id}", dependentId)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }
}
