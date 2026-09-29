package ee.sportclub.persistence.joinapplication;

import ee.sportclub.controller.joinapplication.dto.JoinApplicationResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface JoinApplicationMapper {

    JoinApplicationResponse toJoinApplicationResponse(String message);
}