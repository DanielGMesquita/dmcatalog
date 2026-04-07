package dev.danielmesquita.dmcatalog.dto;

import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.services.validation.UserInsertValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@UserInsertValid
public class UserInsertDTO extends UserDTO {

  @NotBlank(message = "Password must not be empty")
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$",
      message = "Password must contain at least one letter and one number")
  @Size(min = 6, max = 12, message = "Password must be between 6 and 12 characters")
  private String password;

  public UserInsertDTO() {}

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
