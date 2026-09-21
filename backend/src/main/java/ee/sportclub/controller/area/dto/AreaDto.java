package ee.sportclub.controller.area.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link ee.sportclub.persistence.area.Area}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AreaDto implements Serializable {
    Integer areaId;
    @NotNull
    @Size(max = 255)
    String areaName;
}