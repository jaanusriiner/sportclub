package ee.sportclub.controller.usersport.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link ee.sportclub.persistence.usersport.UserSport}
 */
@Value
public class UserSportDto implements Serializable {
    Integer id;
    Integer sportId;
    Integer userId;
}