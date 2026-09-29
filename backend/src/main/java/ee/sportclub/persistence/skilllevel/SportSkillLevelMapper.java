package ee.sportclub.persistence.skilllevel;

import ee.sportclub.controller.sport.SportSkillLevelDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SportSkillLevelMapper {

    @Mapping(source = "id", target = "skillLevelId")
    @Mapping(source = "name", target = "skillLevelName")
    SportSkillLevelDto tosSportSkillLevelDto(SkillLevel skillLevel);

    List<SportSkillLevelDto> tosSportSkillLevelDtos(List<SkillLevel> skillLevels);

}