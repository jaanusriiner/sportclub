package ee.sportclub.controller.register;

import ee.sportclub.controller.register.dto.RegisterRequestDto;
import ee.sportclub.service.register.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class RegisterController {

    private final RegisterService registerService;


    @PostMapping("/api/register")
    public void registerUser(@RequestBody @Valid RegisterRequestDto registerRequestDto){
        registerService.registerUser(registerRequestDto);
    }

}
