package ee.sportclub.controller.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyTrainingDto {

    private Integer trainingDateId;
    private Integer trainingGroupId;
    private String sportName;
    private String facilityName;
    private String trainerName;
    private LocalDate nextTrainingDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime nextTrainingTime;
    private Integer userCount;
    private Integer maxSize;

}
