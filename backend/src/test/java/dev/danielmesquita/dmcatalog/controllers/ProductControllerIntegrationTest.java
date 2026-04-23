package dev.danielmesquita.dmcatalog.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danielmesquita.dmcatalog.dto.ProductDTO;
import dev.danielmesquita.dmcatalog.utils.Factory;
import dev.danielmesquita.dmcatalog.utils.TokenUtil;
import java.time.Instant;
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

  @Autowired private TokenUtil tokenUtil;

  private ProductDTO productDTO;

  private Long existingId;
  private Long nonExistingId;
  private Long countTotalProducts;

  private String adminToken;
  private String clientToken;
  private String invalidToken;

  private String adminUsername;
  private String clientUsername;
  private String invalidUsername;

  private String password;
  private String invalidPassword;

  @BeforeEach
  public void setUp() throws Exception {
    existingId = 1L; // Assume this ID exists in the test database
    nonExistingId = 1000L; // Assume this ID does not exist
    countTotalProducts = 25L; // Assume there are 25 products in total
    productDTO = Factory.createProductDTO();

    adminUsername = "maria@gmail.com";
    clientUsername = "alex@gmail.com";
    password = "123456";
    invalidToken = "00000000";
    adminToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, password);
    clientToken = tokenUtil.obtainAccessToken(mockMvc, clientUsername, password);
  }

  @Test
  @WithMockUser
  public void findAllShouldReturnSortedPageOrderedByName() throws Exception {
    ResultActions resultActions =
        mockMvc.perform(
            get("/products?page0&size=12&sort=name,asc").accept(MediaType.APPLICATION_JSON));

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
        mockMvc.perform(
            get("/products?page=0&size=12&name=mac").accept(MediaType.APPLICATION_JSON));

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
            get("/products?page0&size=12&categoryId=1,3&sort=name,asc")
                .accept(MediaType.APPLICATION_JSON));

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

  @Test
  public void insertShouldReturnProductDTOCreatedWhenAdminLogged() throws Exception {
    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
        mockMvc.perform(
            post("/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()));

    resultActions.andExpect(status().isCreated());
    resultActions.andExpect(jsonPath("$.id").exists());
    resultActions.andExpect(jsonPath("$.name").value(productDTO.getName()));
  }

  @Test
  public void insertShouldReturnExceptionWhenClientLogged() throws Exception {
    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
        mockMvc.perform(
            post("/products")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()));

    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void insertShouldReturnExceptionWhenInvalidToken() throws Exception {
    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
        mockMvc.perform(
            post("/products")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()));

    resultActions.andExpect(status().isUnauthorized());
  }

  @Test
  public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidName()
      throws Exception {
    productDTO.setName("ab");

    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
        mockMvc.perform(
            post("/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()));

    resultActions.andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidDescription()
      throws Exception {
    productDTO.setDescription("");

    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
        mockMvc.perform(
            post("/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()));

    resultActions.andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidPrice()
      throws Exception {
    productDTO.setPrice(-10.0);

    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
        mockMvc.perform(
            post("/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()));

    resultActions.andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidDate()
      throws Exception {
    productDTO.setDate(Instant.now().plusSeconds(3600));

    String jsonBody = objectMapper.writeValueAsString(productDTO);

    ResultActions resultActions =
        mockMvc.perform(
            post("/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()));

    resultActions.andExpect(status().isUnprocessableEntity());
  }
}
