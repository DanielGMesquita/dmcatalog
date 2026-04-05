package dev.danielmesquita.dmcatalog.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danielmesquita.dmcatalog.dto.ProductDTO;
import dev.danielmesquita.dmcatalog.utils.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private Long existingId;
  private Long nonExistingId;
  private Long countTotalProducts;

  @BeforeEach
  public void setUp() {
    existingId = 1L; // Assume this ID exists in the test database
    nonExistingId = 1000L; // Assume this ID does not exist
    countTotalProducts = 25L; // Assume there are 25 products in total
  }

  @Test
  @WithMockUser
  public void findAllShouldReturnSortedPageOrderedByName() throws Exception {
    ResultActions resultActions =
        mockMvc.perform(get("/products?page0&size=12").accept(MediaType.APPLICATION_JSON));

    resultActions.andExpect(status().isOk());
    resultActions.andExpect(jsonPath("$.content").exists());
    resultActions.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
    resultActions.andExpect(jsonPath("$.content[1].name").value("PC Gamer"));
    resultActions.andExpect(jsonPath("$.content[2].name").value("PC Gamer Alfa"));
    resultActions.andExpect(jsonPath("$.page.totalElements").value(countTotalProducts));
  }

  @Test
  @WithMockUser
  public void findAllShouldReturnSortedPageOrderedByNameWhenNameHasValue() throws Exception {
    ResultActions resultActions =
        mockMvc.perform(get("/products?page0&size=12&name=mac").accept(MediaType.APPLICATION_JSON));

    resultActions.andExpect(status().isOk());
    resultActions.andExpect(jsonPath("$.content").exists());
    resultActions.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
    resultActions.andExpect(jsonPath("$.page.totalElements").value(1L));
  }

  @Test
  @WithMockUser
  public void findAllShouldReturnSortedPageOrderedByNameWhenCategoryIdHasValue() throws Exception {
    ResultActions resultActions =
        mockMvc.perform(
            get("/products?page0&size=12&categoryId=1,3").accept(MediaType.APPLICATION_JSON));

    resultActions.andExpect(status().isOk());
    resultActions.andExpect(jsonPath("$.content").exists());
    resultActions.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
    resultActions.andExpect(jsonPath("$.content[1].name").value("PC Gamer"));
    resultActions.andExpect(jsonPath("$.content[2].name").value("PC Gamer Alfa"));
    resultActions.andExpect(jsonPath("$.page.totalElements").value(countTotalProducts - 2L));
  }

  @Test
  @WithMockUser
  public void findByIdShouldReturnProductWhenIdExists() throws Exception {
    ResultActions resultActions =
        mockMvc.perform(get("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON));

    resultActions.andExpect(status().isOk());
    resultActions.andExpect(jsonPath("$.id").value(existingId));
    resultActions.andExpect(jsonPath("$.name").value("The Lord of the Rings"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
    ProductDTO productDTO = Factory.createProductDTO();
    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
        mockMvc.perform(
            put("/products/{id}", existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()));

    resultActions.andExpect(status().isOk());
    resultActions.andExpect(jsonPath("$.id").value(existingId));
    resultActions.andExpect(jsonPath("$.name").value(productDTO.getName()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
    ProductDTO productDTO = Factory.createProductDTO();
    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
        mockMvc.perform(
            put("/products/{id}", nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()));

    resultActions.andExpect(status().isNotFound());
  }
}
