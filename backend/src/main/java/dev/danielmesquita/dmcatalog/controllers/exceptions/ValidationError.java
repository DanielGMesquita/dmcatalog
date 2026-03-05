package dev.danielmesquita.dmcatalog.controllers.exceptions;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class ValidationError extends StandardError {
  @Serial
  private static final long serialVersionUID = 1L;

  private final List<FieldMessage> errors = new ArrayList<>();

  public List<FieldMessage> getErrors() {
    return errors;
  }

  public void addError(String field, String message) {
    errors.add(new FieldMessage(field, message));
  }
}
