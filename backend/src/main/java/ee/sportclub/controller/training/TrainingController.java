package ee.sportclub.controller.training;


import ee.sportclub.controller.training.dto.TrainingGroupOverviewPageDto;
import ee.sportclub.service.training.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
