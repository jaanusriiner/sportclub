package ee.sportclub.persistence.user;

import ee.sportclub.controller.user.dto.UserManagementDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select u from User u where u.email = :email and u.password = :password and u.status = :status")
    Optional<User> findUserBy(String email, String password, String status);

    @Query("select (count(u) > 0) from User u where upper(u.email) = upper(?1) and u.status = ?2")
    boolean userEmailIsTakenByActiveUser(String email, String status);

    @Query("""
            select new ee.sportclub.controller.user.dto.UserManagementDto(
                u.id, u.email, p.firstName, p.lastName, p.phoneNumber, u.role.name, u.status)
            from User u
            left join Profile p on p.user.id = u.id
            order by u.id""")
    List<UserManagementDto> findUserManagementDtos();
}