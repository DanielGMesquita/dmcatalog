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
  private Long countTotalCategories;

  @BeforeEach
  public void setUp() {
    existingId = 1L;
    nonExistingId = 1000L;
    countTotalCategories = 3L;
  }

  // ── GET /categories ───────────────────────────────────────────────────────

  @Test
  @WithMockUser
  @DisplayName("findAll deve retornar página com total correto")
  public void findAllDeveRetornarPaginaComTotalCorreto() throws Exception {
    ResultActions result = mockMvc.perform(get("/categories")
            .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.content").exists());
    result.andExpect(jsonPath("$.totalElements").value(countTotalCategories));
  }

  @Test
  @WithMockUser
  @DisplayName("findAll deve retornar categorias ordenadas por nome quando sort=name,asc")
  public void findAllDeveRetornarCategoriasOrdenadasPorNome() throws Exception {
    ResultActions result = mockMvc.perform(
            get("/categories?page=0&size=10&sort=name,asc")
                    .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.content[0].name").value("Computadores"));
    result.andExpect(jsonPath("$.content[1].name").value("Eletrônicos"));
    result.andExpect(jsonPath("$.content[2].name").value("Livros"));
  }

  // ── GET /categories/{id} ──────────────────────────────────────────────────

  @Test
  @WithMockUser
  @DisplayName("findById deve retornar CategoryDTO quando id existe")
  public void findByIdDeveRetornarCategoryDTOQuandoIdExiste() throws Exception {
    ResultActions result = mockMvc.perform(get("/categories/{id}", existingId)
            .accept(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.id").value(existingId));
    result.andExpect(jsonPath("$.name").value("Livros"));
  }

  @Test
  @WithMockUser
  @DisplayName("findById deve retornar 404 com body de erro quando id não existe")
  public void findByIdDeveRetornar404ComBodyDeErroQuandoIdNaoExiste() throws Exception {
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
  @DisplayName("insert deve retornar 201 com Location header e body quando dados válidos")
  public void insertDeveRetornar201ComLocationEBodyQuandoDadosValidos() throws Exception {
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
  @DisplayName("insert sem role ADMIN deve retornar 403")
  public void insertSemRoleAdminDeveRetornar403() throws Exception {
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
  @DisplayName("update deve retornar CategoryDTO atualizado quando id existe")
  public void updateDeveRetornarCategoryDTOAtualizadoQuandoIdExiste() throws Exception {
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
  @DisplayName("update deve retornar 404 quando id não existe")
  public void updateDeveRetornar404QuandoIdNaoExiste() throws Exception {
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
  @DisplayName("delete deve retornar 404 quando id não existe")
  public void deleteDeveRetornar404QuandoIdNaoExiste() throws Exception {
    mockMvc.perform(delete("/categories/{id}", nonExistingId)
                    .with(csrf()))
            .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("delete sem role ADMIN deve retornar 403")
  public void deleteSemRoleAdminDeveRetornar403() throws Exception {
    mockMvc.perform(delete("/categories/{id}", existingId)
                    .with(csrf()))
            .andExpect(status().isForbidden());
  }
}
