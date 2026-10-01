package ee.sportclub.persistence.training.trainingdate;

import ee.sportclub.controller.training.dto.TrainingGroupOverviewDto;
import ee.sportclub.controller.user.dto.MyTrainingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TrainingDateOverviewRepository extends JpaRepository<TrainingDateOverview, Integer> {
    @Query(value = """
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
            order by v.trainingDate, v.trainingTime""",
            countQuery = """
                    select count(v) from TrainingDateOverview v
                    where (:areaId = 0 or v.areaId = :areaId)
                      and (:sportId = 0 or v.sportId = :sportId)
                      and (:trainerId = 0 or v.trainerId = :trainerId)
                      and v.trainingDate >= :dateFrom
                      and v.trainingTime >= :timeFrom""")
    Page<TrainingGroupOverviewDto> findTrainingGroupOverviewDtosBy(Integer userId, Integer areaId, Integer sportId, Integer trainerId, LocalDate dateFrom, LocalTime timeFrom, Pageable pageable);

    @Query(value = """
                   select new ee.sportclub.controller.user.dto.MyTrainingDto(
                       v.trainingDateId, v.trainingGroupId, v.sportName,
                           v.facilityName, v.trainerName, v.trainingDate,
                               v.trainingTime, v.userCount, v.maxSize
                       )
                   from TrainingDateOverview v
                       where exists (
                           select 1 from UserTraining ut
                               where ut.trainingDate.id = v.trainingDateId
                                   and ut.user.id = :userId
                           )
                               and (v.trainingDate > LOCAL_DATE or (v.trainingDate = LOCAL_DATE and v.trainingTime > LOCAL_TIME))
                                   order by v.trainingDate, v.trainingTime
            """)
    List<MyTrainingDto> findUpcomingUserTrainingDtosBy(Integer userId);
}