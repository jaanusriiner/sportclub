package ee.sportclub.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserTrainingRepository extends JpaRepository<UserTraining, Integer> {
    @Query("select (count(u) > 0) from UserTraining u where u.user.id = :userId and u.trainingDate.id = :trainingDateId")
    boolean userIsRegisteredToTraining(Integer userId, Integer trainingDateId);

    @Modifying
    @Query("delete from UserTraining u where u.user.id = :userId and u.trainingDate.id = :trainingDateId")
    int deleteUserTrainingBy(Integer userId, Integer trainingDateId);

    @Modifying
    @Query("delete from UserTraining u where u.trainingDate.id = :trainingDateId")
    void deleteUserTrainingsBy(Integer trainingDateId);
}
