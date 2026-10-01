package ee.sportclub.controller.training.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTrainingRequestDto {

    @NotNull
    private Integer trainerId;
    @NotNull
    private Integer trainingGroupId;
    @NotNull
    private Integer facilityId;
    @NotNull
    @Pattern(regexp = "[ETKNRLP](,[ETKNRLP])*", message = "lubatud väärtused on E,T,K,N,R,L,P")
    private String weekdays;
    @NotNull
    private LocalTime startTime;
    @NotNull
    @Min(1)
    private Integer duration;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    @Min(1)
    private Integer maxSize;
    @Size(max = 255)
    private String description;

    @AssertTrue(message = "Lõpu kuupäev ei tohi olla enne alguse kuupäeva")
    @JsonIgnore
    public boolean isEndDateNotBeforeStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }


}