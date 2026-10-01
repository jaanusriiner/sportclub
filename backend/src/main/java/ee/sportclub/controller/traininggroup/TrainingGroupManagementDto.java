package ee.sportclub.controller.traininggroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingGroupManagementDto implements Serializable {
    Integer trainingGroupId;
    String trainingGroupName;
    String sportclubName;
    String sportName;
    String skillLevelName;
    String trainerName;
    Integer memberCount;
    Integer trainingCount;
}
