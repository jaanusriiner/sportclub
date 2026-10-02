package ee.sportclub.controller.facility;

import ee.sportclub.controller.facility.dto.CreateFacilityRequestDto;
import ee.sportclub.controller.facility.dto.FacilityDto;
import ee.sportclub.service.facility.FacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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


    @PostMapping("/api/facilities")
    @Operation(
            summary = "Lisab treeningfacility asukoha"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            - "'message': Ei leidnud primary keyd 'adminId'"
                            - "'message': Ei leidnud primary keyd 'areaId'"
                            - "'message': Ei leidnud primary keyd 'sportId'"
                            """
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            - "'message': Selle toimingu jaoks on vaja administraatori õigusi"
                            - "'message': Vali vähemalt üks spordiala"
                            - "'message': Sellise nimega asukoht on juba olemas"
                            """
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            - "'message': facilityName: ei tohi olla tühi"
                            - "'message': <väli>: ei tohi olla tühi"
                            - "'message': imageData: lubatud on ainult JPEG ja PNG pildid"
                            - "'message': imageData: pildi maksimaalne suurus on 10 MB"
                            - "'message': facilityName ei tohi olla pikem kui 60 tähemärki"
                            - "'message': address ei tohi olla pikem kui 90 tähemärki"
                            """
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    }
    )
    public void addFacilityLocation(@RequestBody @Valid CreateFacilityRequestDto createFacilityRequestDto) {
        facilityService.addFacilityLocation(createFacilityRequestDto);

    }
}
