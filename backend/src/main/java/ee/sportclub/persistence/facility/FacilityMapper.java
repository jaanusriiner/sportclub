package ee.sportclub.persistence.facility;

import ee.sportclub.controller.facility.dto.FacilityDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FacilityMapper {

    @Mapping(source = "id", target = "facilityId")
    @Mapping(source = "name", target = "facilityName")
    @Mapping(source = "address", target = "facilityAddress")
    @Mapping(source = "area.id", target = "areaId")
    FacilityDto toFacilityDto(Facility facility);

    List<FacilityDto> toFacilityDtos(List<Facility> facilities);
}
