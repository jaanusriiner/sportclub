package ee.sportclub.controller.facility.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateFacilityRequestDto {

    @NotNull
    private Integer adminId;

    @NotNull
    private Integer areaId;

    @NotEmpty
    @Size(min=1, max=60)
    private String facilityName;

    @NotEmpty
    @Size(min=1, max=90)
    private String address;

    @Size(max=255)
    private String description;

    @NotNull
    private List<Integer> sportIds;

    @Size(max=14_000_000, message = "pildi maksimaalne suurus on 10 MB")
    @Pattern(regexp = "data:image/jpeg;base64,.*|data:image/png;base64,.*|", message = "lubatud on ainult JPEG ja PNG pildid")
    private String imageData;
}