package ee.sportclub.controller.tone;

import ee.sportclub.service.tone.ToneService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ToneController {

    private final ToneService toneService;

    @GetMapping("/api/tones")
    @Operation(summary = "Vastuse toonide pärimine",
            description = "Tagastatakse Benchmark-vaates valitavate vastuse toonide list (id järgi sorteeritud). Toonide puudumisel tagastatakse tühi list.")
    public List<ToneDto> findTones() {
        return toneService.findTones();
    }
}
