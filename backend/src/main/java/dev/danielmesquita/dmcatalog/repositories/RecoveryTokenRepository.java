package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.RecoveryToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecoveryTokenRepository extends JpaRepository<RecoveryToken, Long> {}
