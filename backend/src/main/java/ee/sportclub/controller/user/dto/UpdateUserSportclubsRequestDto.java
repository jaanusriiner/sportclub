package ee.sportclub.controller.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserSportclubsRequestDto {

    @NotNull
    private Integer adminId;

    @NotNull
    private List<Integer> sportclubIds;
}
