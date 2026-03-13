package dev.danielmesquita.dmcatalog.enums;

public enum RoleEnum {
  ROLE_ADMIN("ROLE_ADMIN"),
  ROLE_OPERATOR("ROLE_OPERATOR");

  private final String authority;

  RoleEnum(String authority) {
    this.authority = authority;
  }

  public String getAuthority() {
    return authority;
  }
}
