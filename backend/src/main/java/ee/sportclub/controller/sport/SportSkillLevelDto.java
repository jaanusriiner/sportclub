package ee.sportclub.controller.sport;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link ee.sportclub.persistence.skilllevel.SkillLevel}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SportSkillLevelDto implements Serializable {
    Integer skillLevelId;
    @NotNull
    @Size(max = 30)
    String skillLevelName;
}