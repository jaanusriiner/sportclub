package ee.sportclub.controller.register.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {
    @NotNull
    @Email
    String email;
    @NotNull
    String password;
    @NotNull
    @Size(max = 255)
    String firstName;
    @NotNull
    @Size(max = 255)
    String lastName;
    @NotNull
    Integer phoneNumber;
    @NotNull
    Integer areaId;
    @NotNull
    List<Integer> sportIds;

}
