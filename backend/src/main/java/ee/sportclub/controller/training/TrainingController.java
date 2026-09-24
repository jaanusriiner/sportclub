package ee.sportclub.controller.training;


import ee.sportclub.controller.training.dto.TrainingGroupOverviewDto;
import ee.sportclub.infrastructure.error.ApiError;
import ee.sportclub.service.training.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping("/api/trainings")
    @Operation(summary = "Leiab kõik treeningud",
            description = """
                    -userId on kohustuslik
                    -Kõik teised parameetrid on valikulised, kui edastatakse tühi väärtus siis piirangut ei rakendata
                    -time parameeter tagastab treeningud ALATES määratud kellaajast
                    -date parameeter määrab ära täpse kuupäeva"""
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404", description = "Kui userId on tundmatu siis kuvatakse 'message': Tundmatu kasutaja, 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })

    public List<TrainingGroupOverviewDto> findTrainings(
            @RequestParam Integer userId,
            @RequestParam(required = false) Integer areaId,
            @RequestParam(required = false) Integer sportId,
            @RequestParam(required = false) Integer trainerId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalTime time
    )
    {
    return trainingService.findTrainings(userId, areaId, sportId, trainerId, date, time);

    }

}
