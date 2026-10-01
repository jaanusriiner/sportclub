package ee.sportclub.controller.facility;

import ee.sportclub.controller.facility.dto.FacilityDto;
import ee.sportclub.service.facility.FacilityService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping("/api/facilities")
    @Operation(summary = "Kõikide asukohtade info pärimine",
            description = "Tagastatakse kõikide asukohtade (facility) list nime järgi sorteeritult. Asukohtade puudumisel tagastatakse tühi list"
    )
    public List<FacilityDto> findFacilities() {
        return facilityService.findFacilities();
    }

}
