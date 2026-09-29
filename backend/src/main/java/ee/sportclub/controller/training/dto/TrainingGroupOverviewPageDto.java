package ee.sportclub.controller.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingGroupOverviewPageDto implements Serializable {

    private List<TrainingGroupOverviewDto> trainings;
    private Long totalElements;
    private Integer totalPages;

}
