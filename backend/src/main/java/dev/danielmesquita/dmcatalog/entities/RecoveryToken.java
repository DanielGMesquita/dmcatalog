package dev.danielmesquita.dmcatalog.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tb_recovery_token")
public class RecoveryToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String token;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private Instant expiration;

  public Long getId() {
    return id;
  }

  public String getToken() {
    return token;
  }

  public String getEmail() {
    return email;
  }

  public Instant getExpiration() {
    return expiration;
  }

  public void setExpiration(Instant expiration) {
    this.expiration = expiration;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RecoveryToken that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
