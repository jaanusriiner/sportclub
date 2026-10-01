package ee.sportclub.controller.user;

import ee.sportclub.controller.user.dto.UpdateUserRequestDto;
import ee.sportclub.controller.user.dto.UpdateUserSportclubsRequestDto;
import ee.sportclub.controller.user.dto.UserManagementDto;
import ee.sportclub.service.user.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping("/api/users")
    @Operation(summary = "Kõigi kasutajate nimekiri (ainult admin)",
            description = "Tagastab kõik kasutajad koos profiili, rolli, staatuse ja treeneri spordiklubide id-dega.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "'message': Selle toimingu jaoks on vaja administraatori õigusi"),
            @ApiResponse(responseCode = "404", description = "'message': Ei leidnud primary keyd 'userId'")
    })
    public List<UserManagementDto> findUsers(@RequestParam Integer adminId) {
        return userManagementService.findUsers(adminId);
    }

    @PutMapping("/api/users/{userId}")
    @Operation(summary = "Kasutaja rolli ja staatuse muutmine (ainult admin)",
            description = "Staatus D tähendab deaktiveeritud kasutajat, kes ei saa sisse logida. Andmed jäävad alles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "'message': roleName/status ei ole lubatud väärtus"),
            @ApiResponse(responseCode = "403", description = """
                    - "'message': Selle toimingu jaoks on vaja administraatori õigusi"
                    - "'message': Iseenda rolli ega staatust ei saa muuta"
                    - "'message': Sellise kasutajanimega (email) aktiivne kasutaja on juba süsteemis olemas"
                    """),
            @ApiResponse(responseCode = "404", description = "'message': Ei leidnud primary keyd 'userId'")
    })
    public void updateUser(@PathVariable Integer userId, @RequestBody @Valid UpdateUserRequestDto updateUserRequestDto) {
        userManagementService.updateUser(userId, updateUserRequestDto);
    }

    @PutMapping("/api/users/{userId}/sportclubs")
    @Operation(summary = "Treeneri spordiklubide määramine (ainult admin)",
            description = "Asendab treeneri senised spordiklubid saadetud nimekirjaga.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = """
                    - "'message': Selle toimingu jaoks on vaja administraatori õigusi"
                    - "'message': Spordiklubidega saab siduda ainult treeneri rolliga kasutajat"
                    """),
            @ApiResponse(responseCode = "404", description = "'message': Ei leidnud primary keyd 'userId' / 'sportclubId'")
    })
    public void updateUserSportclubs(@PathVariable Integer userId,
                                     @RequestBody @Valid UpdateUserSportclubsRequestDto updateUserSportclubsRequestDto) {
        userManagementService.updateUserSportclubs(userId, updateUserSportclubsRequestDto);
    }
}
