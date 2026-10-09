package ee.sportclub.persistence.llmmodel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LlmModelRepository extends JpaRepository<LlmModel, Integer> {

    @Query("select l from LlmModel l where l.isActive = true order by l.provider, l.name")
    List<LlmModel> findActiveLlmModels();
}
