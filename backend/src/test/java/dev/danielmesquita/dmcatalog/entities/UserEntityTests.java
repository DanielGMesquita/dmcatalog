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
    user = Factory.createUser(); // has ROLE_OPERATOR and ROLE_ADMIN
  }

  @Test
  @DisplayName("hasRole should return true when the user has the given role")
  public void hasRoleShouldReturnTrueWhenUserHasRole() {
    assertTrue(user.hasRole(RoleEnum.ROLE_OPERATOR));
    assertTrue(user.hasRole(RoleEnum.ROLE_ADMIN));
  }

  @Test
  @DisplayName("hasRole should return false when the user does not have the given role")
  public void hasRoleShouldReturnFalseWhenUserDoesNotHaveRole() {
    User userWithoutRole = new User();
    userWithoutRole.setId(2L);
    userWithoutRole.setFirstName("Test");
    userWithoutRole.setLastName("User");
    userWithoutRole.setEmail("test@test.com");
    userWithoutRole.setPassword("pass");
    // no roles added
    assertFalse(userWithoutRole.hasRole(RoleEnum.ROLE_ADMIN));
    assertFalse(userWithoutRole.hasRole(RoleEnum.ROLE_OPERATOR));
  }

  @Test
  @DisplayName("RoleEnum should return the correct authority for each value")
  public void roleEnumShouldReturnCorrectAuthority() {
    assertEquals("ROLE_ADMIN", RoleEnum.ROLE_ADMIN.getAuthority());
    assertEquals("ROLE_OPERATOR", RoleEnum.ROLE_OPERATOR.getAuthority());
  }
}
