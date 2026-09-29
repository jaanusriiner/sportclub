package ee.sportclub.persistence.joinapplication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JoinApplicationRepository extends JpaRepository<JoinApplication, Integer> {


    @Query("select j from JoinApplication j where j.userId = :userId and j.trainingGroupId = :trainingGroupId")
    Optional<JoinApplication> findJoinApplicationBy(Integer userId,  Integer trainingGroupId);

boolean existsByUserIdAndTrainingGroupIdAndStatus(Integer userId, Integer trainingGroupId, String status);
}
