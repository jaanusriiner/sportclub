package ee.sportclub.persistence.training.trainingdate;

import ee.sportclub.persistence.training.Training;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TrainingDateRepository extends JpaRepository<TrainingDate, Integer> {
    @Query("""
            select t from TrainingDate t
            where (t.startDate > LOCAL_DATE or (t.startDate >= LOCAL_DATE and t.startTime > LOCAL_TIME))
                        and (:sportId = 0 or t.training.trainingGroup.sport.id = :sportId)
                                    and (cast(:areaId as integer) is null or t.facility.area.id = :areaId)
                                                and (cast(:trainerId as integer) is null or t.training.trainingGroup.user.id = :trainerId)
                                                            and (cast(:date as date) is null or t.startDate = :date)
                                                                        and (cast(:time as time) is null or t.startTime >= :time)
            order by t.startDate, t.startTime""")
    List<TrainingDate> findTrainingBy(Integer areaId, Integer sportId, Integer trainerId, LocalDate date, LocalTime time);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TrainingDate t where t.id = :trainingDateId")
    Optional<TrainingDate> findTrainingDateByIdAndLockIt(Integer trainingDateId);

    List<TrainingDate> training(Training training);

    @Modifying
    @Query("""
            delete from TrainingDate t
            where t.training.id in (select tr.id from Training tr where tr.trainingGroup.id = :trainingGroupId)""")
    void deleteTrainingDatesBy(Integer trainingGroupId);

    // review tabelil pole entity't, seepärast native päring
    @Modifying
    @Query(value = """
            delete from sportclub.review r
            using sportclub.training_date td, sportclub.training t
            where r.training_date_id = td.id
              and td.training_id = t.id
              and t.training_group_id = :trainingGroupId""", nativeQuery = true)
    void deleteReviewsBy(Integer trainingGroupId);
}