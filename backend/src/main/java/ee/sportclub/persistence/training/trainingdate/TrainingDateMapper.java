package ee.sportclub.persistence.training.trainingdate;


import ee.sportclub.controller.training.dto.TrainingGroupOverviewDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingDateMapper {

    @Mappings({
            @Mapping(source = "training.trainingGroup.id", target = "trainingGroupId"),
            @Mapping(source = "training.trainingGroup.sport.id", target = "sportId"),
            @Mapping(source = "training.trainingGroup.sport.name", target = "sportName"),
            @Mapping(source = "facility.id", target = "facilityId"),
            @Mapping(source = "facility.name", target = "facilityName"),
            @Mapping(source = "facility.area.id", target = "areaId"),
            @Mapping(source = "training.trainingGroup.user.id", target = "trainerId"),
            @Mapping(ignore = true, target = "trainerName"),
            @Mapping(source = "training.trainingGroup.sportclub.id", target = "sportclubId"),
            @Mapping(source = "training.trainingGroup.sportclub.name", target = "sportclubName"),
            @Mapping(source = "training.trainingGroup.skillLevel.id", target = "skillLevelId"),
            @Mapping(source = "training.trainingGroup.skillLevel.name", target = "skillLevelName"),
            @Mapping(source = "id", target = "trainingDateId"),
            @Mapping(source = "startDate", target = "trainingDate"),
            @Mapping(source = "startTime", target = "trainingTime"),
            @Mapping(source = "status", target = "status"),
            @Mapping(source = "userCount", target = "userCount"),
            @Mapping(source = "maxSize", target = "maxSize"),
            @Mapping(ignore = true, target = "userIsRegistered"),
            @Mapping(ignore = true, target = "userIsTrainingGroupMember")

    })
    TrainingGroupOverviewDto toTrainingGroupOverviewDto(TrainingDate trainingDate);

    List<TrainingGroupOverviewDto> toTrainingGroupOverviewDtos(List<TrainingDate> trainingDates);
}
