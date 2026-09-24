package ee.sportclub.persistence.training;

import ee.sportclub.controller.training.dto.TrainingGroupOverviewDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TrainingDateOverviewRepository extends JpaRepository<TrainingDateOverview, Integer> {
    @Query("""
            select new ee.sportclub.controller.training.dto.TrainingGroupOverviewDto(
                v.trainingGroupId, v.sportId, v.sportName, v.facilityId, v.facilityName, v.areaId,
                v.trainerId, v.trainerName, v.sportclubId, v.sportclubName,
                v.skillLevelId, v.skillLevelName, v.trainingDateId,
                v.trainingDate, v.trainingTime, v.status, v.userCount, v.maxSize,
                case when exists (select 1 from UserTraining ut
                                  where ut.trainingDate.id = v.trainingDateId
                                    and ut.user.id = :userId) then true else false end,
                case when exists (select 1 from UserTrainingGroup utg
                                  where utg.trainingGroup.id = v.trainingGroupId
                                    and utg.user.id = :userId) then true else false end)
            from TrainingDateOverview v
            where (:areaId = 0 or v.areaId = :areaId)
              and (:sportId = 0 or v.sportId = :sportId)
              and (:trainerId = 0 or v.trainerId = :trainerId)
              and v.trainingDate >= :dateFrom
              and v.trainingTime >= :timeFrom
            order by v.trainingDate, v.trainingTime""")
    List<TrainingGroupOverviewDto> findTrainingGroupOverviewDtosBy(Integer userId, Integer areaId, Integer sportId, Integer trainerId, LocalDate dateFrom, LocalTime timeFrom);
}
