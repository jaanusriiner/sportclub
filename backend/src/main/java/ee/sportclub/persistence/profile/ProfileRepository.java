package ee.sportclub.persistence.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    Set<Profile> findByUserId(Set<Integer> trainerIds);
}