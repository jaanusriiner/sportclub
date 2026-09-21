package ee.sportclub.controller.area;


import ee.sportclub.controller.area.dto.AreaDto;
import ee.sportclub.service.area.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;


    @GetMapping("/api/areas")
    @Operation(summary = "Kõikide piirkondade info pärimine",
            description = "Tagastatakse kõikide piirkondade list. Piirkondade puudumisel tagastatakse tühi list"
    )
    public List<AreaDto> findAreas() {
        List<AreaDto> areaDtos = areaService.findAreas();
        return areaDtos;
    }


}
