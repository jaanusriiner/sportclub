package ee.sportclub.controller.trainer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link ee.sportclub.persistence.sportclubtrainer.SportclubTrainer}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerSportclubDto implements Serializable {
    Integer sportclubId;
    String sportclubName;
}