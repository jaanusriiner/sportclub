package ee.sportclub.persistence.training;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainingGroupRepository extends JpaRepository<TrainingGroup, Integer> {

    @Query("select t from TrainingGroup t where t.user.id = :trainerId")
    List<TrainingGroup> findByTrainerId(Integer trainerId);
}