package ee.sportclub.controller.sportclub;

import ee.sportclub.controller.sportclub.dto.SportclubDto;
import ee.sportclub.controller.sportclub.dto.SportclubTrainerDto;
import ee.sportclub.service.sportclub.SportclubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SportclubController {

    private final SportclubService sportclubService;

    @GetMapping("/api/sportclubs")
    @Operation(summary = "Kõigi spordiklubide nimekiri",
            description = "Tagastatakse kõik spordiklubid nime järgi sorteeritult.")
    public List<SportclubDto> findSportclubs() {
        return sportclubService.findSportclubs();
    }

    @GetMapping("/api/sportclubs/{sportclubId}/trainers")
    @Operation(summary = "Spordiklubi treenerite nimekirja päring",
            description = "Tagastatakse spordiklubis tegevate treenerite nimekiri (kasutab admin treeninggrupi loomisel treeneri valimiseks). Treenerite puudumisel tagastatakse tühi list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Ei leidnud primary keyd 'sportclubId'")
    })
    public List<SportclubTrainerDto> findSportclubTrainers(@PathVariable Integer sportclubId) {
        return sportclubService.findSportclubTrainers(sportclubId);
    }
}
