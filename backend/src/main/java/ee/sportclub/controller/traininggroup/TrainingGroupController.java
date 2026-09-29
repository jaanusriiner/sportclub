package ee.sportclub.controller.traininggroup;

import ee.sportclub.service.traininggroup.TrainingGroupService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequiredArgsConstructor
class TrainingGroupController {

    private final TrainingGroupService trainingGroupService;

    @PostMapping("/api/training-groups")
    @Operation(summary = "Treeninggrupi registreerimine",
            description = "Luuakse uus treeninggrupp")
    public void addTrainingGroup(@RequestBody @Valid TrainingGroupDto trainingGroupDto){
        trainingGroupService.addTrainingGroup(trainingGroupDto);
    }



}
