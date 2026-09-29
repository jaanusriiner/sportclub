package ee.sportclub.persistence.training;

import ee.sportclub.controller.traininggroup.TrainingGroupDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingGroupMapper {
    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "user")
    @Mapping(ignore = true, target = "sport")
    @Mapping(ignore = true, target = "skillLevel")
    @Mapping(ignore = true, target = "sportclub")
    @Mapping(source = "trainingGroupName", target = "name")
    @Mapping(source = "description", target = "description")
    TrainingGroup toTrainingGroup(TrainingGroupDto trainingGroupDto);

}