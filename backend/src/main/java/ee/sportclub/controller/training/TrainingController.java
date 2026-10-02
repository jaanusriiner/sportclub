package ee.sportclub.controller.training;


import ee.sportclub.controller.training.dto.*;
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
                    description = "'message': Ei leidnud primary keyd 'trainingDateId', 'errorCode': 'PRIMARY_KEY_NOT_FOUND'",
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
    public TrainingRegisterResponseDto registerToTraining(@PathVariable Integer trainingDateId, @RequestBody @Valid TrainingDateRegisterRequestDto trainingDateRegisterRequestDto) {
        return trainingService.registerToTraining(trainingDateId, trainingDateRegisterRequestDto);


    }

    @PostMapping("/api/trainings")
    @Operation(
            summary = "Loob uue treeningseeria koos treeningkordadega",
            description = """
                    - Vajab sisse request bodyt
                    - {
                    -   "trainerId": Int,
                    -   "trainingGroupId": Int,
                    -   "facilityId": Int,
                    -   "weekdays": "E,N",
                    -   "startTime": "18:45",
                    -   "duration": Int,
                    -   "startDate": "2026-10-05",
                    -   "endDate": "2026-10-15",
                    -   "maxSize": Int,
                    -   "description": ""
                    - }
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            - 'message': Ei leidnud primary keyd 'trainerId'
                            - 'message': Ei leidnud primary keyd 'trainingGroupId'
                            - 'message': Ei leidnud primary keyd 'facilityId'
                            """,
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            - 'message': Antud treeninggrupp ei kuulu antud treenerile
                            - 'message': Valitud perioodi ei jää ühtegi valitud nädalapäeva
                            """,
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            - 'message': Lõpu kuupäev ei tohi olla enne alguse kuupäeva
                            - 'message': 'weekdays': lubatud väärtused on 'E,T,K,N,R,L,P'
                            - 'message': 'maxSize': peab olema suurem kui'0'
                            - 'message': '<väli>': ei tohi olla tühi
                            """,
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void createNewTraining(@RequestBody @Valid CreateTrainingRequestDto createTrainingRequestDto) {
        trainingService.createNewTraining(createTrainingRequestDto);
    }

    @DeleteMapping("/api/training-dates/{trainingDateId}/register")
    @Operation(
            summary = "Vabastab kasutaja koha treeningul trainingDateId järgi",
            description = """
                    - Vajab path variable trainingDateId ja query parameetrit userId
                    - Eemaldab kasutaja treeningult ja vähendab treeningu osalejate arvu (user_count) ühe võrra
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "'message': Ei leidnud primary keyd 'trainingDateId' või 'userId', 'errorCode': 'PRIMARY_KEY_NOT_FOUND'",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "'message': Sa ei ole sellele treeningule registreerunud, 'errorCode': 'NOT_REGISTERED'",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public TrainingRegisterResponseDto unregisterFromTraining(@PathVariable Integer trainingDateId, @RequestParam Integer userId) {
        return trainingService.unregisterFromTraining(trainingDateId, userId);
    }

    @DeleteMapping("/api/training-dates/{trainingDateId}")
    @Operation(
            summary = "Kustutab ühe treeningu trainingDateId järgi",
            description = "Ootab sisse trainingDateId"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "'message': Ei leidnud primary keyd 'trainingDateId'",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public void deleteTrainingDate(@PathVariable Integer trainingDateId) {
        trainingService.deleteTrainingDateBy(trainingDateId);
    }

    @PutMapping("/api/training-dates/{trainingDateId}")
    @Operation(
            summary = "Muudab ühe treeningkorra detaile trainingDateId järgi",
            description = "Ootab sisse trainingDateId ja request body"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "'message': Ei leidnud primary keyd 'trainingDateId'",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "'message': Maksimaalne osalejate arv ei tohi olla väiksem juba registreerunud kasutajate arvust",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            ),
    })
    public void updateTrainingDateDetails(@PathVariable Integer trainingDateId, @RequestBody @Valid UpdateTrainingDateRequestDto updateTrainingDateRequestDto) {
        trainingService.updateTrainingDateDetails(trainingDateId, updateTrainingDateRequestDto);
    }


}
