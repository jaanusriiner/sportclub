package ee.sportclub.controller.trainer;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link ee.sportclub.persistence.sportclubtrainer.SportclubTrainer}
 */
@Value
public class TrainerSportclubDto implements Serializable {
    Integer sportclubId;
    String sportclubName;
}