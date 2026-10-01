package ee.sportclub.controller.facility.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link ee.sportclub.persistence.facility.Facility}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilityDto implements Serializable {
    Integer facilityId;
    String facilityName;
    String facilityAddress;
    Integer areaId;
}
