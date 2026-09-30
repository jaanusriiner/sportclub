package ee.sportclub.controller.profile.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link ee.sportclub.persistence.profile.Profile}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto implements Serializable {
    Integer profileId;
    Integer userId;
    @NotNull
    @Size(max = 255)
    String firstName;
    @NotNull
    @Size(max = 255)
    String lastName;
    @NotNull
    Integer phoneNumber;
    Integer areaId;
}