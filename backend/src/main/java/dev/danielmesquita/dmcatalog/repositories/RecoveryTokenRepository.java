package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.RecoveryToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RecoveryTokenRepository extends JpaRepository<RecoveryToken, Long> {

  @Query("SELECT obj FROM RecoveryToken obj WHERE obj.token = :token AND obj.expiration > :now")
  Optional<RecoveryToken> searchValidToken(String token, Instant now);
}
