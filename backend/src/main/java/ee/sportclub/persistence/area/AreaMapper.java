package ee.sportclub.persistence.area;

import ee.sportclub.controller.area.dto.AreaDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AreaMapper {

    @Mapping(source = "id", target = "areaId")
    @Mapping(source = "name", target = "areaName")
    AreaDto toAreaDto(Area area);

   List<AreaDto> toAreaDtos(List<Area> areas);
}