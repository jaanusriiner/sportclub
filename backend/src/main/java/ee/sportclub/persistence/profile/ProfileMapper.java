package ee.sportclub.persistence.profile;

import ee.sportclub.controller.profile.dto.ProfileDto;
import ee.sportclub.controller.register.dto.RegisterRequestDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "area")
    @Mapping(ignore = true, target = "user")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    Profile toProfile(RegisterRequestDto registerRequestDto);
}