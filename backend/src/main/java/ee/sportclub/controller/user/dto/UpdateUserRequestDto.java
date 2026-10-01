package ee.sportclub.controller.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequestDto {

    @NotNull
    private Integer adminId;

    @NotNull
    @Pattern(regexp = "admin|trainer|customer", message = "lubatud on admin, trainer või customer")
    private String roleName;

    @NotNull
    @Pattern(regexp = "A|D", message = "lubatud on A (aktiivne) või D (deaktiveeritud)")
    private String status;
}
