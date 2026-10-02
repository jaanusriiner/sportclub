package ee.sportclub.persistence.facility;

import ee.sportclub.controller.facility.dto.CreateFacilityRequestDto;
import ee.sportclub.controller.facility.dto.FacilityDetailDto;
import ee.sportclub.controller.facility.dto.UpdateFacilityRequestDto;
import ee.sportclub.controller.facility.dto.FacilityDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FacilityMapper {

    @Mapping(source = "id", target = "facilityId")
    @Mapping(source = "name", target = "facilityName")
    @Mapping(source = "address", target = "facilityAddress")
    @Mapping(source = "area.id", target = "areaId")
    @Mapping(source = "description", target = "facilityDescription")
    FacilityDto toFacilityDto(Facility facility);

    List<FacilityDto> toFacilityDtos(List<Facility> facilities);


    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "area")
    @Mapping(source = "facilityName", target = "name")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "description", target = "description")
    Facility toFacility(CreateFacilityRequestDto createFacilityRequestDto);

    @Mapping(source = "id", target = "facilityId")
    @Mapping(source = "name", target = "facilityName")
    @Mapping(source = "area.id", target = "areaId")
    @Mapping(ignore = true, target = "sportIds")
    FacilityDetailDto toFacilityDetailDto(Facility facility);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "area")
    @Mapping(source = "facilityName", target = "name")
    void updateFacility(UpdateFacilityRequestDto updateFacilityRequestDto, @MappingTarget Facility facility);
}
