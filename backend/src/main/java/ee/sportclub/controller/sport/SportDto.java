package ee.sportclub.controller.sport;

import ee.sportclub.persistence.sport.Sport;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Sport}
 */
@Data
@AllArgsConstructor
@Value
public class SportDto implements Serializable {
    Integer sportId;
    @NotNull
    @Size(max = 255)
    String sportName;
}