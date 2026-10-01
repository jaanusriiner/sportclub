package ee.sportclub.persistence.training;

import ee.sportclub.controller.training.dto.CreateTrainingRequestDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingMapper {


    @Mappings({
            @Mapping(ignore = true, target = "trainingGroup"),
            @Mapping(ignore = true, target = "defaultFacility"),
            @Mapping(source = "weekdays", target = "weekdays"),
            @Mapping(source = "startTime", target = "defaultStartTime"),
            @Mapping(source = "duration", target = "duration"),
            @Mapping(source = "startDate", target = "defaultStartDate"),
            @Mapping(source = "endDate", target = "defaultEndDate"),
            @Mapping(source = "maxSize", target = "maxsize"),
            @Mapping(source = "description", target = "description"),
            @Mapping(ignore = true, target = "id"),
            @Mapping(ignore = true, target = "name"),
            @Mapping(ignore = true, target = "defaultEndTime")
    })
    Training toTraining(CreateTrainingRequestDto createTrainingRequestDto);
}
