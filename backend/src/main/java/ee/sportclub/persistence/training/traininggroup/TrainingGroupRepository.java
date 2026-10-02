package ee.sportclub.persistence.training.traininggroup;

import ee.sportclub.controller.traininggroup.TrainingGroupManagementDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrainingGroupRepository extends JpaRepository<TrainingGroup, Integer> {

    @Query("select t from TrainingGroup t where t.user.id = :trainerId")
    List<TrainingGroup> findByTrainerId(Integer trainerId);

    @Query("select (count(t) > 0) from TrainingGroup t where t.user.id = :userId and t.id = :trainingGroupId")
    boolean trainerIsTrainingGroupTrainer(Integer userId, Integer trainingGroupId);

    @Query("""
            select new ee.sportclub.controller.traininggroup.TrainingGroupManagementDto(
                tg.id,
                tg.name,
                tg.sportclub.name,
                tg.sport.name,
                tg.skillLevel.name,
                coalesce(concat(p.firstName, ' ', p.lastName), tg.user.email),
                cast((select count(utg) from UserTrainingGroup utg where utg.trainingGroup.id = tg.id) as Integer),
                cast((select count(t) from Training t where t.trainingGroup.id = tg.id) as Integer)
            )
            from TrainingGroup tg
            left join Profile p on p.user.id = tg.user.id
            where (:trainerId = 0 or tg.user.id = :trainerId)
            order by tg.sportclub.name, tg.name""")
    List<TrainingGroupManagementDto> findTrainingGroupManagementDtosBy(Integer trainerId);
}