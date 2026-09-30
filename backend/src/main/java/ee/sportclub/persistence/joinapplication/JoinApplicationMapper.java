package ee.sportclub.persistence.joinapplication;

import ee.sportclub.controller.joinapplication.dto.JoinApplicationResponse;
import ee.sportclub.controller.joinapplication.dto.PendingJoinApplicationDto;
import org.mapstruct.*;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface JoinApplicationMapper {

    JoinApplicationResponse toJoinApplicationResponse(String message);

    @Mapping(source = "id", target = "joinApplicationId")
    PendingJoinApplicationDto toPendingJoinApplicationDto(JoinApplication joinApplication);

    List<PendingJoinApplicationDto> toPendingJoinApplicationDtos(List<JoinApplication> joinApplications);
}