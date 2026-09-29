package ee.sportclub.controller.trainer;

import ee.sportclub.controller.sport.SportSkillLevelDto;
import ee.sportclub.service.trainer.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
class TrainerController {

    private final TrainerService trainerService;

    @GetMapping("/api/trainers/{trainerId}/sportclubs")
    @Operation(summary = "Treeneri spordiklubide pärimine",
            description = "Tagastatakse spordiklubide nimekiri, kus vastav treener on tegev. Spordiklubide puudumisel tagastatakse tühi list.")
    public List<TrainerSportclubDto> findTrainerSportclubs(@PathVariable Integer trainerId) {

        return trainerService.findTrainerSportclubs(trainerId);
    }

    @GetMapping("/api/trainers/{trainerId}/training-groups")
    @Operation(summary = "Treeneriga seotud treeninggruppide päring",
            description = "Tagastatakse treeninggruppide nimekiri, kus vastav treener on määratud vastutavaks/omanikuks. Treeninggruppide puudumisel tagastatakse tühi list.")
    public List<TrainerTrainingGroupDto> findTrainerTrainingGroups(@PathVariable Integer trainerId) {

        return trainerService.findTrainerTrainingGroups(trainerId);
    }



}
