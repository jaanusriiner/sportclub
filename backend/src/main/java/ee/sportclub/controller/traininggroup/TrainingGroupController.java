package ee.sportclub.controller.traininggroup;

import ee.sportclub.service.traininggroup.TrainingGroupManagementService;
import ee.sportclub.service.traininggroup.TrainingGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
class TrainingGroupController {

    private final TrainingGroupService trainingGroupService;
    private final TrainingGroupManagementService trainingGroupManagementService;

    @GetMapping("/api/training-groups")
    @Operation(summary = "Hallatavate treeninggruppide nimekiri",
            description = "Admin näeb kõiki treeninggruppe, treener ainult enda omi. Igal grupil on liikmete ja treeningute arv.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "'message': Ei leidnud primary keyd 'userId'")
    })
    public List<TrainingGroupManagementDto> findTrainingGroups(@RequestParam Integer userId) {
        return trainingGroupManagementService.findTrainingGroups(userId);
    }

    @DeleteMapping("/api/training-groups/{trainingGroupId}")
    @Operation(summary = "Treeninggrupi kustutamine",
            description = """
                    Kustutab treeninggrupi koos selle treeningute, treeningkordade, registreerumiste,
                    liikmesuste ja liitumistaotlustega. Kasutajaid ega treenereid ei kustutata.
                    Kustutada saab admin või grupi treener.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "'message': Antud treeninggrupp ei kuulu valitud treenerile"),
            @ApiResponse(responseCode = "404", description = "'message': Ei leidnud primary keyd 'trainingGroupId' / 'userId'")
    })
    public void deleteTrainingGroup(@PathVariable Integer trainingGroupId, @RequestParam Integer userId) {
        trainingGroupManagementService.deleteTrainingGroup(trainingGroupId, userId);
    }

    @PostMapping("/api/training-groups")
    @Operation(summary = "Treeninggrupi registreerimine",
            description = "Luuakse uus treeninggrupp")
    public void addTrainingGroup(@RequestBody @Valid TrainingGroupDto trainingGroupDto) {
        trainingGroupService.addTrainingGroup(trainingGroupDto);
    }


}
