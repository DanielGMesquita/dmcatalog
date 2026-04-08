package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.UserDTO;
import dev.danielmesquita.dmcatalog.dto.UserInsertDTO;
import dev.danielmesquita.dmcatalog.dto.UserUpdateDTO;
import dev.danielmesquita.dmcatalog.entities.Role;
import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.enums.RoleEnum;
import dev.danielmesquita.dmcatalog.projections.UserDetailsProjection;
import dev.danielmesquita.dmcatalog.repositories.RoleRepository;
import dev.danielmesquita.dmcatalog.repositories.UserRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.DatabaseException;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {
  private final UserRepository repository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository repository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
    this.repository = repository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    List<UserDetailsProjection> result = repository.findUserDetailsByEmail(username);
    if (result.isEmpty()) {
      throw new UsernameNotFoundException("User not found with email: " + username);
    }
    User user = new User();
    user.setEmail(username);
    user.setPassword(result.getFirst().getPassword());
    result.stream()
        .map(
            projection -> {
              Role role = new Role();
              role.setId(projection.getRoleId());
              role.setAuthority(projection.getAuthority());
              return role;
            })
        .forEach(user::addRole);

    return user;
  }

  @Transactional(readOnly = true)
  public Page<UserDTO> findAllPaged(Pageable pageable) {
    Page<User> list = repository.findAll(pageable);
    return list.map(UserDTO::new);
  }

  @Transactional(readOnly = true)
  public UserDTO findById(Long id) {
    Optional<User> obj = repository.findById(id);
    User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
    return new UserDTO(entity);
  }

  @Transactional
  public UserDTO insert(UserInsertDTO dto) {
    User entity = new User();
    copyDtoToEntity(dto, entity);
    entity.getRoles().add(roleRepository.findByAuthority(RoleEnum.ROLE_OPERATOR.getAuthority()));
    entity.setPassword(passwordEncoder.encode(dto.getPassword()));
    entity = repository.save(entity);
    return new UserDTO(entity);
  }

  @Transactional
  public UserUpdateDTO update(Long id, UserUpdateDTO dto) {
    try {
      User entity = repository.getReferenceById(id);
      copyDtoToEntity(dto, entity);
      User finalEntity = entity;
      dto.getRoles()
          .forEach(
              roleDto -> {
                Role role = roleRepository.getReferenceById(roleDto.getId());
                finalEntity.getRoles().add(role);
              });
      entity = repository.save(entity);
      return new UserUpdateDTO(entity);
    } catch (EntityNotFoundException e) {
      throw new ResourceNotFoundException("Id not found " + id);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new ResourceNotFoundException("Id not found " + id);
    }
    try {
      repository.deleteById(id);
    } catch (DataIntegrityViolationException e) {
      throw new DatabaseException("Integrity violation");
    }
  }

  private void copyDtoToEntity(UserDTO dto, User entity) {
    entity.setFirstName(dto.getFirstName());
    entity.setLastName(dto.getLastName());
    entity.setEmail(dto.getEmail());
    entity.getRoles().clear();
  }

  @Transactional(readOnly = true)
  public UserDTO getMe() {
    User user = authenticated();
    return new UserDTO(user);
  }

  protected User authenticated() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      Jwt jwt = (Jwt) authentication.getPrincipal();
      String username = jwt.getClaim("username");

      return repository.findByEmail(username);
    } catch (Exception e) {
      throw new UsernameNotFoundException("User not found");
    }
  }
}
