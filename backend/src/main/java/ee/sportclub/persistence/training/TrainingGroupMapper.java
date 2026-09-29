package ee.sportclub.persistence.training;

import ee.sportclub.controller.trainer.TrainerTrainingGroupDto;
import ee.sportclub.controller.traininggroup.TrainingGroupDto;
import org.mapstruct.*;

import java.util.List;

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

    @Mapping(source = "id", target = "trainingGroupId")
    @Mapping(source = "name", target = "trainingGroupName")
    @Mapping(source = "sportclub.id", target = "sportclubId")
    @Mapping(source = "sportclub.name", target = "sportclubName")
    TrainerTrainingGroupDto toTrainerTrainingGroupDto(TrainingGroup trainingGroup);


    List<TrainerTrainingGroupDto> toTrainerTrainingGroupDtos(List<TrainingGroup> trainingGroups);

}