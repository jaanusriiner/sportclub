package ee.sportclub.persistence.joinapplication;

import ee.sportclub.controller.joinapplication.dto.JoinApplicationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface JoinApplicationMapper {

    @Mapping(ignore = true, target = "id")
    @Mapping(constant = "PEN", target = "status")
    JoinApplication toEntity(Integer trainingGroupId, JoinApplicationRequest request);
}