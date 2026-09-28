package ee.sportclub.persistence.sportclubtrainer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SportclubTrainerRepository extends JpaRepository<SportclubTrainer, Integer> {


    @Query("select s from SportclubTrainer s where s.user.id = :trainerId")
    List<SportclubTrainer> findSportClubsByTrainer(Integer trainerId);
}