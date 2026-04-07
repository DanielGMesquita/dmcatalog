package dev.danielmesquita.dmcatalog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailDTO {

  @NotBlank(message = "Email must not be empty")
  @Email(message = "Email should be valid")
  private String email;

  public EmailDTO() {}

  public EmailDTO(String email) {
    this.email = email;
  }

  public String getEmail() {
    return this.email;
  }
}
