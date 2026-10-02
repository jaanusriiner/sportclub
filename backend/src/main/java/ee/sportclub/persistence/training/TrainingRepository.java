package ee.sportclub.persistence.training;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TrainingRepository extends JpaRepository<Training, Integer> {

    @Modifying
    @Query("delete from Training t where t.trainingGroup.id = :trainingGroupId")
    void deleteTrainingsBy(Integer trainingGroupId);

    @Query("select (count(t) > 0) from Training t where t.defaultFacility.id = :facilityId")
    boolean facilityIsUsedInTrainings(Integer facilityId);
}
