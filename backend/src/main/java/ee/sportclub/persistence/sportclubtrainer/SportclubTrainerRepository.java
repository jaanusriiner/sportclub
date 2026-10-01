package ee.sportclub.persistence.sportclubtrainer;

import ee.sportclub.controller.sportclub.dto.SportclubTrainerDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SportclubTrainerRepository extends JpaRepository<SportclubTrainer, Integer> {


    @Query("select s from SportclubTrainer s where s.user.id = :trainerId")
    List<SportclubTrainer> findSportClubsByTrainer(Integer trainerId);

    @Query("select (count(s) > 0) from SportclubTrainer s where s.user.id = :userId and s.sportclub.id = :sportclubId")
    boolean existsSportclubByTrainerId(Integer userId, Integer sportclubId);

    @Query("""
            select new ee.sportclub.controller.sportclub.dto.SportclubTrainerDto(
                s.user.id,
                coalesce(concat(p.firstName, ' ', p.lastName), s.user.email)
            )
            from SportclubTrainer s
            left join Profile p on p.user.id = s.user.id
            where s.sportclub.id = :sportclubId
            order by p.firstName, p.lastName
            """)
    List<SportclubTrainerDto> findSportclubTrainerDtosBy(Integer sportclubId);

    @Modifying
    @Query("delete from SportclubTrainer s where s.user.id = :userId")
    void deleteSportclubTrainersBy(Integer userId);


}