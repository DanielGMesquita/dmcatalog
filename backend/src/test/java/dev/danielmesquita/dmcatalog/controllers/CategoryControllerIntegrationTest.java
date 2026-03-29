package dev.danielmesquita.dmcatalog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danielmesquita.dmcatalog.dto.CategoryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private Long existingId;
  private Long nonExistingId;

  @BeforeEach
  public void setUp() {
    existingId = 1L;
    nonExistingId = 1000L;
  }

  // ── GET /categories ───────────────────────────────────────────────────────

  @Test
  @WithMockUser
  public void findAllShouldReturnAllCategories() throws Exception {
    ResultActions resultActions = mockMvc.perform(get("/categories")
            .accept(MediaType.APPLICATION_JSON));

    resultActions.andExpect(status().isOk());
    resultActions.andExpect(jsonPath("$.content[0].id").value(existingId.toString()));
  }

  // ── GET /categories/{id} ──────────────────────────────────────────────────

  @Test
  @WithMockUser
  @DisplayName("findById should return CategoryDTO when id exists")
  public void findByIdShouldReturnCategoryDTOWhenIdExists() throws Exception {
    ResultActions result = mockMvc.perform(get("/categories/{id}", existingId)
            .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.id").value(existingId));
    result.andExpect(jsonPath("$.name").value("Livros"));
  }

  @Test
  @WithMockUser
  @DisplayName("findById should return 404 with error body when id does not exist")
  public void findByIdShouldReturn404WithErrorBodyWhenIdDoesNotExist() throws Exception {
    ResultActions result = mockMvc.perform(get("/categories/{id}", nonExistingId)
            .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isNotFound());
    result.andExpect(jsonPath("$.status").value(404));
    result.andExpect(jsonPath("$.error").value("Resource not found"));
    result.andExpect(jsonPath("$.path").value("/categories/" + nonExistingId));
  }

  // ── POST /categories ──────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("insert should return 201 with Location header and body when data is valid")
  public void insertShouldReturn201WithLocationAndBodyWhenDataIsValid() throws Exception {
    CategoryDTO dto = new CategoryDTO(null, "Games");
    String jsonBody = objectMapper.writeValueAsString(dto);

    ResultActions result = mockMvc.perform(post("/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody)
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf()));

    result.andExpect(status().isCreated());
    result.andExpect(header().exists("Location"));
    result.andExpect(jsonPath("$.id").exists());
    result.andExpect(jsonPath("$.name").value("Games"));
  }

  @Test
  @WithMockUser
  @DisplayName("insert without ADMIN role should return 403")
  public void insertWithoutAdminRoleShouldReturn403() throws Exception {
    CategoryDTO dto = new CategoryDTO(null, "Games");
    String jsonBody = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(csrf()))
            .andExpect(status().isForbidden());
  }

  // ── PUT /categories/{id} ──────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("update should return updated CategoryDTO when id exists")
  public void updateShouldReturnUpdatedCategoryDTOWhenIdExists() throws Exception {
    CategoryDTO dto = new CategoryDTO(existingId, "Livros Atualizados");
    String jsonBody = objectMapper.writeValueAsString(dto);

    ResultActions result = mockMvc.perform(put("/categories/{id}", existingId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody)
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf()));

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.id").value(existingId));
    result.andExpect(jsonPath("$.name").value("Livros Atualizados"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("update should return 404 when id does not exist")
  public void updateShouldReturn404WhenIdDoesNotExist() throws Exception {
    CategoryDTO dto = new CategoryDTO(nonExistingId, "Inexistente");
    String jsonBody = objectMapper.writeValueAsString(dto);

    mockMvc.perform(put("/categories/{id}", nonExistingId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(csrf()))
            .andExpect(status().isNotFound());
  }

  // ── DELETE /categories/{id} ───────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("delete should return 404 when id does not exist")
  public void deleteShouldReturn404WhenIdDoesNotExist() throws Exception {
    mockMvc.perform(delete("/categories/{id}", nonExistingId)
                    .with(csrf()))
            .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("delete without ADMIN role should return 403")
  public void deleteWithoutAdminRoleShouldReturn403() throws Exception {
    mockMvc.perform(delete("/categories/{id}", existingId)
                    .with(csrf()))
            .andExpect(status().isForbidden());
  }
}
