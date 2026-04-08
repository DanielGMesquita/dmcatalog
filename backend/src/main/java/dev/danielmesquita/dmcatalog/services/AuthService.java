package dev.danielmesquita.dmcatalog.services;

import dev.danielmesquita.dmcatalog.dto.EmailDTO;
import dev.danielmesquita.dmcatalog.dto.NewPasswordDTO;
import dev.danielmesquita.dmcatalog.entities.RecoveryToken;
import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.repositories.RecoveryTokenRepository;
import dev.danielmesquita.dmcatalog.repositories.UserRepository;
import dev.danielmesquita.dmcatalog.services.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  @Value("${email.recovery-token.duration}")
  private Long tokenDuration;

  @Value("${email.recovery-token.uri}")
  private String recoveryUri;

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RecoveryTokenRepository tokenRepository;
  private final EmailService emailService;

  public AuthService(
      PasswordEncoder passwordEncoder,
      UserRepository userRepository,
      RecoveryTokenRepository tokenRepository,
      EmailService emailService,
      UserService userService) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.tokenRepository = tokenRepository;
    this.emailService = emailService;
  }

  @Transactional
  public void createRecoveryToken(EmailDTO requestBody) {
    String email = requestBody.getEmail();
    User user = userRepository.findByEmail(email);

    if (user == null) {
      throw new ResourceNotFoundException("User not found with email: " + email);
    }

    RecoveryToken recoveryToken = new RecoveryToken();
    recoveryToken.setEmail(email);
    recoveryToken.setToken(UUID.randomUUID().toString());
    recoveryToken.setExpiration(Instant.now().plusSeconds(tokenDuration));
    tokenRepository.save(recoveryToken);

    String emailBody =
        """
            Dear %s,

            We received a request to reset your password. Please access the following link to reset your password:
            %s

            This link will expire in %d minutes.

            If you did not request a password reset, please ignore this email.

            Best regards,
            DMCatalog Team
            """
            .formatted(
                user.getFirstName(), recoveryUri + recoveryToken.getToken(), tokenDuration / 60);

    emailService.sendEmail(email, "Password recovery", emailBody);
  }

  @Transactional
  public void saveNewPassword(NewPasswordDTO body) {
    RecoveryToken validToken =
        tokenRepository
            .searchValidToken(body.getToken(), Instant.now())
            .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired token"));

    User user = userRepository.findByEmail(validToken.getEmail());
    user.setPassword(passwordEncoder.encode(body.getPassword()));
    userRepository.save(user);
  }
}
