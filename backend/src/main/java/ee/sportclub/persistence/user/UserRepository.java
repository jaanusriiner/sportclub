package ee.sportclub.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select u from User u where u.email = :email and u.password = :password and u.status = :status")
    Optional<User> findUserBy(String email, String password, String status);

    @Query("select (count(u) > 0) from User u where upper(u.email) = upper(?1) and u.status = ?2")
    boolean userEmailIsTakenByActiveUser(String email, String status);
}