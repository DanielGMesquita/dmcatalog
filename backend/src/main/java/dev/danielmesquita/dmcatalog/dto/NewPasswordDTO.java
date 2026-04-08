package dev.danielmesquita.dmcatalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NewPasswordDTO {
  @NotBlank(message = "Token must not be empty")
  private String token;

  @NotBlank(message = "Password must not be empty")
  @Size(min = 6, message = "Password must be at least 6 characters long")
  private String password;

  public NewPasswordDTO() {}

  public NewPasswordDTO(String token, String password) {
    this.token = token;
    this.password = password;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
