package ee.sportclub.controller.login;

import ee.sportclub.controller.login.dto.LoginRequestDto;
import ee.sportclub.controller.login.dto.LoginResponseDto;
import ee.sportclub.service.login.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/api/login")
    @Operation(summary = "Sisse logimine, tagastab userId ja roleName")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Ebaõnnestunud sisselogimisel kuvatakse -> 'message:' Vale e-post või parool; 'errorCode': INCORRECT_CREDENTIALS"
            ),
            @ApiResponse(
                    responseCode = "500"
            )
    })
    public LoginResponseDto loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponseDto = loginService.loginUser(loginRequestDto);
        return loginResponseDto;

    }

}
