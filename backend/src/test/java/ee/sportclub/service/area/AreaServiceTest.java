package ee.sportclub.service.area;

import ee.sportclub.controller.area.dto.AreaDto;
import ee.sportclub.persistence.area.Area;
import ee.sportclub.persistence.area.AreaMapper;
import ee.sportclub.persistence.area.AreaMapperImpl;
import ee.sportclub.persistence.area.AreaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AreaServiceTest {

    @Mock
    private AreaRepository areaRepository;

    private AreaService areaService;

    @BeforeEach
    void setUp() {
        AreaMapper areaMapper = new AreaMapperImpl();
        areaService = new AreaService(areaRepository, areaMapper);
    }

    @Test
    void findAreas_requestsAreasSortedByNameAscending() {
        when(areaRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of());

        areaService.findAreas();

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(areaRepository).findAll(sortCaptor.capture());
        Sort.Order nameOrder = sortCaptor.getValue().getOrderFor("name");
        assertEquals(Sort.Direction.ASC, nameOrder.getDirection());
        assertEquals(1, sortCaptor.getValue().stream().count());
    }

    @Test
    void findAreas_returnsAllAreasInRepositoryOrderWithMappedFields() {
        List<Area> areas = List.of(
                createArea(1, "Harjumaa"),
                createArea(4, "Hiiumaa"),
                createArea(2, "Läänemaa"),
                createArea(5, "Pärnumaa"),
                createArea(3, "Saaremaa"));
        when(areaRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(areas);

        List<AreaDto> areaDtos = areaService.findAreas();

        assertEquals(5, areaDtos.size());
        assertEquals(List.of("Harjumaa", "Hiiumaa", "Läänemaa", "Pärnumaa", "Saaremaa"),
                areaDtos.stream().map(AreaDto::getAreaName).toList());
        assertEquals(List.of(1, 4, 2, 5, 3),
                areaDtos.stream().map(AreaDto::getAreaId).toList());
    }

    @Test
    void findAreas_returnsEmptyListWhenNoAreasExist() {
        when(areaRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of());

        List<AreaDto> areaDtos = areaService.findAreas();

        assertTrue(areaDtos.isEmpty());
    }

    private Area createArea(Integer areaId, String areaName) {
        Area area = new Area();
        area.setId(areaId);
        area.setName(areaName);
        return area;
    }
}
