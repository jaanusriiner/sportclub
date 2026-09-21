package ee.sportclub.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {


    @Query("select (count(u) > 0) from User u where upper(u.email) = upper(?1) and u.status = ?2")
    boolean userEmailIsTakenByActiveUser(String email, String status);
}