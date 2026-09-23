package ee.sportclub.persistence.user;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface UserTrainingGroupRepository extends JpaRepository<UserTrainingGroup, Integer> {
    @Query("select u.trainingGroup.id from UserTrainingGroup u where u.user.id = :id")
    Set<Integer> findTrainingGroupIdsByUserId(Integer id);

}
