package dev.danielmesquita.dmcatalog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danielmesquita.dmcatalog.dto.UserDTO;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

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
  @WithMockUser
  public void findAllShouldReturnSortedPageWhenSortByName() throws Exception {
    ResultActions resultActions = mockMvc.perform(get("/users?page0&size=12&sort=firstName,asc")
            .accept(MediaType.APPLICATION_JSON));

    resultActions.andExpect(status().isOk());
    resultActions.andExpect(jsonPath("$.content").exists());
    resultActions.andExpect(jsonPath("$.content[0].firstName").value("Alex"));
    resultActions.andExpect(jsonPath("$.content[1].firstName").value("Maria"));
    resultActions.andExpect(jsonPath("$.totalElements").value(countTotalUsers));
  }

  @Test
  @WithMockUser
  public void findByIdShouldReturnUserWhenIdExists() throws Exception {
    ResultActions resultActions = mockMvc.perform(get("/users/{id}", existingId)
            .accept(MediaType.APPLICATION_JSON));

    resultActions.andExpect(status().isOk());
    resultActions.andExpect(jsonPath("$.id").value(existingId));
    resultActions.andExpect(jsonPath("$.firstName").value("Alex"));
  }

  @Test
  @WithMockUser
  public void updateShouldReturnUserDTOWhenIdExists() throws Exception {
    UserDTO userDTO = Factory.createUserDTO();
    String jsonBody = objectMapper.writeValueAsString(userDTO);

    ResultActions resultActions = mockMvc.perform(put("/users/{id}", existingId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody)
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf()));

    resultActions.andExpect(status().isOk());
    resultActions.andExpect(jsonPath("$.id").value(existingId));
    resultActions.andExpect(jsonPath("$.firstName").value(userDTO.getFirstName()));
  }

  @Test
  @WithMockUser
  public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
    UserDTO userDTO = Factory.createUserDTO();
    String jsonBody = objectMapper.writeValueAsString(userDTO);

    ResultActions resultActions = mockMvc.perform(put("/users/{id}", nonExistingId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody)
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf()));

    resultActions.andExpect(status().isNotFound());
  }
}
