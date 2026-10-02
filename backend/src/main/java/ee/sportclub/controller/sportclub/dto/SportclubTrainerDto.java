package ee.sportclub.controller.sportclub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SportclubTrainerDto implements Serializable {
    Integer trainerId;
    String trainerName;
}
