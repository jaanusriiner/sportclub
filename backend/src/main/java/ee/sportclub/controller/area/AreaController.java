package ee.sportclub.controller.area;


import ee.sportclub.controller.area.dto.AreaDto;
import ee.sportclub.service.area.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;


    @GetMapping("/api/areas")
    public List<AreaDto> findAreas() {
        List<AreaDto> areaDtos = areaService.findAreas();
        return areaDtos;
    }


}
