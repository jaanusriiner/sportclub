package ee.sportclub.controller.facility.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilityDetailDto implements Serializable {
    Integer facilityId;
    String facilityName;
    String address;
    String description;
    Integer areaId;
    List<Integer> sportIds;
}
