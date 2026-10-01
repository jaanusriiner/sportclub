package ee.sportclub.persistence.training.traininggroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrainingGroupRepository extends JpaRepository<TrainingGroup, Integer> {

    @Query("select t from TrainingGroup t where t.user.id = :trainerId")
    List<TrainingGroup> findByTrainerId(Integer trainerId);

    @Query("select (count(t) > 0) from TrainingGroup t where t.user.id = :userId and t.id = :trainingGroupId")
    boolean trainerIsTrainingGroupTrainer(Integer userId, Integer trainingGroupId);
}