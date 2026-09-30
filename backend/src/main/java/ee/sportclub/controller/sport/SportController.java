package ee.sportclub.controller.sport;

import ee.sportclub.service.sport.SportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequiredArgsConstructor
public class SportController {

    private final SportService sportService;

    @GetMapping("/api/sports")
    @Operation(summary = "Kõikide spordialade pärimine",
            description = "Tagastatakse kõikide spordialade list. Spordialade puudumisel tagastatakse tühi list.")
    public List<SportDto> findSports() {
        return sportService.findSports();
    }

    @GetMapping("/api/sports/{sportId}/skill-levels")
    @Operation(summary = "Spordiala skill-levelite pärimine",
            description = "Tagastatakse spordialade skill-levelite list. Skill-levelite puudumisel tagastatakse tühi list.")
    public List<SportSkillLevelDto> findSportSkillLevels(@PathVariable Integer sportId) {

        return sportService.findSportSkillLevels(sportId);
    }

}
