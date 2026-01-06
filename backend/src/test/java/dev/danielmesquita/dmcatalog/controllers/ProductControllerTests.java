package dev.danielmesquita.dmcatalog.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.danielmesquita.dmcatalog.dto.ProductDTO;
import dev.danielmesquita.dmcatalog.services.ProductService;
import dev.danielmesquita.dmcatalog.utils.Factory;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Classe de teste para o controlador ProductController, utilizando MockMvc para simular requisições
 * HTTP e Mockito para mockar o serviço. WebMvcTest carrega o contexto somente para a camada web,
 * sem carregar o service e repository
 */
@WebMvcTest(ProductController.class)
public class ProductControllerTests {

  /**
   * MockMvc é uma classe do Spring que permite simular requisições HTTP em testes de integração
   * para controladores web.
   */
  @Autowired private MockMvc mockMvc;

  @MockitoBean private ProductService service;

  private ProductDTO productDTO;

  private PageImpl<ProductDTO> page;

  @BeforeEach
  public void setUp() {
    productDTO = Factory.createProductDTO();
    page = new PageImpl<>(List.of(productDTO));

    Mockito.when(service.findAllPaged(ArgumentMatchers.any())).thenReturn(page);
  }

  @Test
  @WithMockUser
  public void findAllShouldReturnPage() throws Exception {
    mockMvc
        .perform(get("/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").exists());
  }
}
