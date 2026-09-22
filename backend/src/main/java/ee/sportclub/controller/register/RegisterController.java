package ee.sportclub.controller.register;

import ee.sportclub.controller.register.dto.RegisterRequestDto;
import ee.sportclub.service.register.RegisterService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class RegisterController {

    private final RegisterService registerService;


    //todo Jaanus täienda dokumentatsioooni osa, veakoodid jne
    @PostMapping("/api/register")
    @Operation(summary = "Kasutajaks registreerimine",
            description = "Uus kasutaja luuakse vaikimisi CUSTOMER rollis. Kasutaja loomise käigus kontrollitakse, kas sama emailiga aktiivne kasutaja on juba süsteemis olemas. ")
    public void registerUser(@RequestBody @Valid RegisterRequestDto registerRequestDto){
        registerService.registerUser(registerRequestDto);
    }

}
