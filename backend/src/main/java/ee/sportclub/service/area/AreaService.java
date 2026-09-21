package ee.sportclub.service.area;


import ee.sportclub.controller.area.dto.AreaDto;
import ee.sportclub.persistence.area.Area;
import ee.sportclub.persistence.area.AreaMapper;
import ee.sportclub.persistence.area.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;
    private final AreaMapper areaMapper;

    public List<AreaDto> findAreas() {
        List<Area> areas = areaRepository.findAll();
        List<AreaDto> areaDtos = areaMapper.toAreaDtos(areas);
        return areaDtos;
    }


}
