package ee.sportclub.persistence.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    @Query("select p from Profile p where p.user.id in :userIds")
    Set<Profile> findByUserIds(Set<Integer> userIds);

}