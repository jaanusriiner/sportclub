package ee.sportclub.persistence.user;

import ee.sportclub.controller.login.dto.LoginResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {


    @Mapping(source = "id", target = "userId")
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(target = "userFullName", ignore = true)
    LoginResponseDto toLoginResponseDto(User user);

}
