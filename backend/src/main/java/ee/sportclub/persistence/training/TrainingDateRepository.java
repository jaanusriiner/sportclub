package ee.sportclub.persistence.training;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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


}