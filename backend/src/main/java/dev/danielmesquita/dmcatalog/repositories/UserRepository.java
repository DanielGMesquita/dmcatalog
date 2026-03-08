package dev.danielmesquita.dmcatalog.repositories;

import dev.danielmesquita.dmcatalog.entities.User;
import dev.danielmesquita.dmcatalog.projections.UserDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  public User findByEmail(String email);

  @Query(
          nativeQuery = true,
          value =
                  """
                            SELECT tb_user.email AS username, tb_user.password, tb_role.id AS role_id, tb_role.authority
                            FROM tb_user
                            INNER JOIN tb_user_role ON tb_user.id = tb_user_role.user_id
                            INNER JOIN tb_role ON tb_role.id = tb_user_role.role_id
                            WHERE tb_user.email = :email
                          """)
  List<UserDetailsProjection> findUserDetailsByEmail(String email);
}
