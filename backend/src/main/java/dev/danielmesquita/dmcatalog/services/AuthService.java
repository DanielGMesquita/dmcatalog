package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.EmailDTO;
import dev.danielmesquita.dmcatalog.entities.RecoveryToken;
import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.repositories.RecoveryTokenRepository;
import dev.danielmesquita.dmcatalog.repositories.UserRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  @Value("${email.recovery-token.duration}")
  private Long tokenDuration;

  private final UserRepository userRepository;
  private final RecoveryTokenRepository tokenRepository;

  public AuthService(UserRepository userRepository, RecoveryTokenRepository tokenRepository) {
    this.userRepository = userRepository;
    this.tokenRepository = tokenRepository;
  }

  @Transactional
  public void createRecoveryToken(EmailDTO body) {
    String email = body.getEmail();
    User user = userRepository.findByEmail(email);

    if (user == null) {
      throw new ResourceNotFoundException("User not found with email: " + email);
    }

    RecoveryToken recoveryToken = new RecoveryToken();
    recoveryToken.setEmail(email);
    recoveryToken.setToken(UUID.randomUUID().toString());
    recoveryToken.setExpiration(Instant.now().plusSeconds(tokenDuration));

    tokenRepository.save(recoveryToken);
  }
}
