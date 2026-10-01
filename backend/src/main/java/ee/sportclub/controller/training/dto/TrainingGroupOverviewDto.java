package ee.sportclub.controller.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingGroupOverviewDto implements Serializable {

    private Integer trainingGroupId;
    private Integer sportId;
    private String sportName;
    private Integer facilityId;
    private String facilityName;
    private Integer areaId;
    private Integer trainerId;
    private String trainerName;
    private Integer sportclubId;
    private String sportclubName;
    private Integer skillLevelId;
    private String skillLevelName;
    private Integer trainingDateId;
    private LocalDate trainingDate;
    private LocalTime trainingTime;
    private String status;
    private Integer userCount;
    private Integer maxSize;
    private Boolean userIsRegistered;
    private Boolean userIsTrainingGroupMember;
    private Boolean userHasPendingJoinApplication;

}
