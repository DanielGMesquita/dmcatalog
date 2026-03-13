package dev.danielmesquita.dmcatalog.entities;

import dev.danielmesquita.dmcatalog.enums.RoleEnum;
import dev.danielmesquita.dmcatalog.utils.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserEntityTests {

  private User user;

  @BeforeEach
  public void setUp() {
    user = Factory.createUser(); // tem ROLE_OPERATOR e ROLE_ADMIN
  }

  @Test
  @DisplayName("hasRole deve retornar true quando usuário possui a role informada")
  public void hasRoleDeveRetornarTrueQuandoUsuarioPossuiARole() {
    assertTrue(user.hasRole(RoleEnum.ROLE_OPERATOR));
    assertTrue(user.hasRole(RoleEnum.ROLE_ADMIN));
  }

  @Test
  @DisplayName("hasRole deve retornar false quando usuário não possui a role informada")
  public void hasRoleDeveRetornarFalseQuandoUsuarioNaoPossuiARole() {
    User userSemRole = new User();
    userSemRole.setId(2L);
    userSemRole.setFirstName("Test");
    userSemRole.setLastName("User");
    userSemRole.setEmail("test@test.com");
    userSemRole.setPassword("pass");
    // sem roles adicionadas
    assertFalse(userSemRole.hasRole(RoleEnum.ROLE_ADMIN));
    assertFalse(userSemRole.hasRole(RoleEnum.ROLE_OPERATOR));
  }

  @Test
  @DisplayName("RoleEnum deve retornar a authority correta para cada valor")
  public void roleEnumDeveRetornarAuthorityCorreta() {
    assertEquals("ROLE_ADMIN", RoleEnum.ROLE_ADMIN.getAuthority());
    assertEquals("ROLE_OPERATOR", RoleEnum.ROLE_OPERATOR.getAuthority());
  }
}
