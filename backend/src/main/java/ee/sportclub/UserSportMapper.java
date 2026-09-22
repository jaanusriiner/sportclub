package ee.sportclub;

import ee.sportclub.controller.usersport.dto.UserSportDto;
import ee.sportclub.persistence.sport.Sport;
import ee.sportclub.persistence.user.User;
import ee.sportclub.persistence.usersport.UserSport;
import org.mapstruct.*;
import org.mapstruct.control.MappingControl;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserSportMapper {
//    @Mapping(source = "userId", target = "user.id")
//    @Mapping(source = "sportId", target = "sport.id")
//    UserSport toEntity(UserSportDto userSportDto);
//
//    @Mapping(ignore = true, target = "id")
//    @Mapping(source = "id", target = "user")
//    @Mapping(source = "sportId", target = "sport")
//    UserSport toUserSport(User user, Sport sport);
//



}