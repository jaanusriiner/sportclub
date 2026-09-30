package ee.sportclub.persistence.usersport;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSportRepository extends JpaRepository<UserSport, Integer> {
}