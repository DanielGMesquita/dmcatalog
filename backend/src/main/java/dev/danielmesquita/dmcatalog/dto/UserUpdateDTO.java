package dev.danielmesquita.dmcatalog.dto;

import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.services.validation.UserUpdateValid;

import java.io.Serial;

@UserUpdateValid
public class UserUpdateDTO extends UserDTO {
  @Serial
  private static final long serialVersionUID = 1L;

  public UserUpdateDTO() {
  }

  public UserUpdateDTO(User user) {
    super(user);
  }
}
