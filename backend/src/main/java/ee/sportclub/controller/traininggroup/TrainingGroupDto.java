package ee.sportclub.controller.traininggroup;

import ee.sportclub.persistence.training.traininggroup.TrainingGroup;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link TrainingGroup}
 */
@Value
public class TrainingGroupDto implements Serializable {
    Integer sportclubId;
    Integer sportId;
    Integer trainerId;
    @NotNull
    @Size(max = 100)
    String trainingGroupName;
    @Size(max = 255)
    String description;
    Integer skillLevelId;
}