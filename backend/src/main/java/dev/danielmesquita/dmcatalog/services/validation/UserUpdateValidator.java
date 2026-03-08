package dev.danielmesquita.dmcatalog.services.validation;

import dev.danielmesquita.dmcatalog.controllers.exceptions.FieldMessage;
import dev.danielmesquita.dmcatalog.dto.UserUpdateDTO;
import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.repositories.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {

  private final UserRepository userRepository;

  public UserUpdateValidator(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void initialize(UserUpdateValid constraintAnnotation) {
  }

  @Override
  public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {
    List<FieldMessage> list = new ArrayList<>();

    User findByEmail = userRepository.findByEmail(dto.getEmail());

    if (findByEmail != null) {
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
