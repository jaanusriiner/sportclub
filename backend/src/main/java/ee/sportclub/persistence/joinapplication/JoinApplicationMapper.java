package ee.sportclub.persistence.joinapplication;

import ee.sportclub.controller.joinapplication.dto.JoinApplicationRequest;
import ee.sportclub.persistence.traininggroup.TrainingGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface JoinApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "trainingGroup.id", target = "trainingGroupId")
    @Mapping(source = "request.userId", target = "userId")
    @Mapping(target = "status", constant = "PEN")
    JoinApplication toJoinApplication(TrainingGroup trainingGroup, JoinApplicationRequest request);
}