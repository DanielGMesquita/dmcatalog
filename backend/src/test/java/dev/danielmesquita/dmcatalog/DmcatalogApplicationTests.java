package dev.danielmesquita.dmcatalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DmcatalogApplicationTests {

  @Test
  void contextLoads() {
  }

  @Test
  @DisplayName("main deve iniciar a aplicação sem lançar exceção")
  void mainDeveIniciarAplicacaoSemExcecao() {
    DmcatalogApplication.main(new String[]{});
  }
}
