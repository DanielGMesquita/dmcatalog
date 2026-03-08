package dev.danielmesquita.dmcatalog.services.validation;

import dev.danielmesquita.dmcatalog.controllers.exceptions.FieldMessage;
import dev.danielmesquita.dmcatalog.dto.UserUpdateDTO;
import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.servlet.HandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {

  private final HttpServletRequest request;

  private final UserRepository userRepository;

  public UserUpdateValidator(UserRepository userRepository, HttpServletRequest request) {
    this.request = request;
    this.userRepository = userRepository;
  }

  @Override
  public void initialize(UserUpdateValid constraintAnnotation) {
  }

  @Override
  public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {
    Object uriVars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    long userId = uriVars instanceof Map ? Long.parseLong((String) ((Map<?, ?>) uriVars).get("id")) : 0L;

    List<FieldMessage> list = new ArrayList<>();

    User findByEmail = userRepository.findByEmail(dto.getEmail());

    if (findByEmail != null && findByEmail.getId() != userId) {
      list.add(new FieldMessage("email", "Email already exists"));
    }

    for (FieldMessage fieldMessage : list) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(fieldMessage.getMessage())
              .addPropertyNode(fieldMessage.getFieldName())
              .addConstraintViolation();
    }

    return list.isEmpty();
  }
}
