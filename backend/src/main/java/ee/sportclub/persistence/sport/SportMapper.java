package ee.sportclub.persistence.sport;

import ee.sportclub.controller.sport.SportDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SportMapper {

    @Mapping(source = "id", target = "sportId")
    @Mapping(source = "name", target = "sportName")
    SportDto toSportDto(Sport sport);

    List<SportDto> toSportDtos(List<Sport> sports);
}