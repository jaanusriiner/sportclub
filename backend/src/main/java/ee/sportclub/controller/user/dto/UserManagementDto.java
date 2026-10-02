package ee.sportclub.controller.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class UserManagementDto implements Serializable {
    Integer userId;
    String email;
    String firstName;
    String lastName;
    Integer phoneNumber;
    String roleName;
    String status;
    // täidetakse service'is, ainult treeneritel
    List<Integer> sportclubIds = new ArrayList<>();

    // kasutatakse UserRepository JPQL konstruktori päringus
    public UserManagementDto(Integer userId, String email, String firstName, String lastName,
                             Integer phoneNumber, String roleName, String status) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.roleName = roleName;
        this.status = status;
    }
}
