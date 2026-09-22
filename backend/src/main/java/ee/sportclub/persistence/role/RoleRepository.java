package ee.sportclub.persistence.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {


    @Query("select r from Role r where upper(r.name) = upper(?1)")
    Optional<Role> findByNameIgnoreCase(String name);
}