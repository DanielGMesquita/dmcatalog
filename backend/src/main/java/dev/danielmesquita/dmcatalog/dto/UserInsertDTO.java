package dev.danielmesquita.dmcatalog.dto;

import dev.danielmesquita.dmcatalog.entities.User;

import java.io.Serial;

public class UserInsertDTO extends UserDTO {

  @Serial
  private static final long serialVersionUID = 1L;

  private String password;

  public UserInsertDTO() {
  }

  public UserInsertDTO(String password) {
    this.password = password;
  }

  public UserInsertDTO(User user) {
    super(user);
    password = user.getPassword();
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
