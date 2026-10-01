package ee.sportclub.controller.training.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTrainingDateRequestDto {

    @Size(max = 255)
    private String description;

    @NotNull
    @Min(1)
    private Integer maxSize;

    @NotNull
    private LocalDate trainingDate;

    @NotNull
    private LocalTime trainingTime;
}
