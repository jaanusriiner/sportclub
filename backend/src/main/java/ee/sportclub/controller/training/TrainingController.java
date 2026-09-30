package ee.sportclub.controller.training;


import ee.sportclub.controller.training.dto.TrainingDateRegisterRequestDto;
import ee.sportclub.controller.training.dto.TrainingGroupOverviewPageDto;
import ee.sportclub.infrastructure.error.ApiError;
import ee.sportclub.service.training.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping("/api/trainings")
    @Operation(summary = "Leiab kõik treeningud",
            description = """
                    - Iga training_date kirje on eraldi rida
                    - requestUserId järgi arvutatakse userIsRegistered ja userIsTrainingGroupMember (0 puhul mõlemad false)
                    - areaId, sportId, trainerId: väärtus 0 tähendab, et selle järgi ei filtreerita
                    - dateFrom tagastab treeningud ALATES määratud kuupäevast
                    - timeFrom tagastab igal päeval treeningud ALATES määratud kellaajast
                    - page on lehekülje number (algab 1-st, vaikimisi 1; väärtus alla 1 tagastab esimese lehe), size on ridade arv leheküljel (vaikimisi 7)"""
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            )
    })
    public TrainingGroupOverviewPageDto findTrainings(
            @RequestParam Integer requestUserId,
            @RequestParam Integer areaId,
            @RequestParam Integer sportId,
            @RequestParam Integer trainerId,
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalTime timeFrom,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "7") Integer size
    ) {
        return trainingService.findTrainings(requestUserId, areaId, sportId, trainerId, dateFrom, timeFrom, page, size);
    }

    @PostMapping("/api/training-dates/{trainingDateId}/register")
    @Operation(
            summary = "Registreerib kasutaja treeningule trainingDateId järgi",
            description = """ 
                    - Vajab sisse trainingDateId ja userId
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "'message': Ei leidnud primary keyd 'trainingDateId' väärtusega: 999', 'errorCode': 'PRIMARY_KEY_NOT_FOUND'",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                    - 'message': Registreerumiseks pead olema treeninggrupi liige, 'errorCode': 'NOT_TRAINING_GROUP_MEMBER'
                    - 'message': Sellel treeningul pole enam vabu kohti, 'errorCode': 'TRAINING_FULL'
                    - 'message': Oled juba sellele treeningule registreerunud, 'errorCode': 'ALREADY_REGISTERED'
                    """,
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
        }
    )
    public void registerToTraining(@PathVariable Integer trainingDateId, @RequestBody @Valid TrainingDateRegisterRequestDto trainingDateRegisterRequestDto) {
        trainingService.registerToTraining(trainingDateId, trainingDateRegisterRequestDto);

    }

}
