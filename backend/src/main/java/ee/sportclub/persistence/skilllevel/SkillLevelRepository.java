package ee.sportclub.persistence.skilllevel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SkillLevelRepository extends JpaRepository<SkillLevel, Integer> {


   @Query("select s from SkillLevel s where s.sport.id = :sportId")
    List<SkillLevel> findBySportId(Integer sportId);
}