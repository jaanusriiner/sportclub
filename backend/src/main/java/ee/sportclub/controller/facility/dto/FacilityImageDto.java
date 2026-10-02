package ee.sportclub.controller.facility.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilityImageDto implements Serializable {
    String imageData;
}
