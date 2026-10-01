package ee.sportclub.persistence.joinapplication;

import ee.sportclub.controller.joinapplication.dto.PendingJoinApplicationDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JoinApplicationRepository extends JpaRepository<JoinApplication, Integer> {

    @Query("select j from JoinApplication j where j.userId = :userId and j.trainingGroupId = :trainingGroupId")
    Optional<JoinApplication> findJoinApplicationBy(Integer userId, Integer trainingGroupId);

    boolean existsByUserIdAndTrainingGroupIdAndStatus(Integer userId, Integer trainingGroupId, String status);

    @Query("""
            select new ee.sportclub.controller.joinapplication.dto.PendingJoinApplicationDto(
                j.id,
                j.userId,
                concat(p.firstName, ' ', p.lastName),
                tg.sportclub.id,
                tg.sportclub.name,
                tg.id,
                tg.name,
                cast((select count(utg) from UserTrainingGroup utg where utg.trainingGroup.id = tg.id) as Integer)
            )
            from JoinApplication j
            join TrainingGroup tg on j.trainingGroupId = tg.id
            join Profile p on j.userId = p.user.id
            where j.status = 'PEN' and tg.user.id = :trainerId
            """)
    List<PendingJoinApplicationDto> findPendingApplicationsByTrainerId(Integer trainerId);

    @Query("""
            select new ee.sportclub.controller.joinapplication.dto.PendingJoinApplicationDto(
                j.id,
                j.userId,
                concat(p.firstName, ' ', p.lastName),
                tg.sportclub.id,
                tg.sportclub.name,
                tg.id,
                tg.name,
                cast((select count(utg) from UserTrainingGroup utg where utg.trainingGroup.id = tg.id) as Integer)
            )
            from JoinApplication j
            join TrainingGroup tg on j.trainingGroupId = tg.id
            join Profile p on j.userId = p.user.id
            where j.status = 'PEN'
            """)
    List<PendingJoinApplicationDto> findAllPendingApplications();

    @Modifying
    @Query("delete from JoinApplication j where j.trainingGroupId = :trainingGroupId")
    void deleteJoinApplicationsBy(Integer trainingGroupId);
}