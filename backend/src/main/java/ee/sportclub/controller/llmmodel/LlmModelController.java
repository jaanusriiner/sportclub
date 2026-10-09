package ee.sportclub.controller.llmmodel;

import ee.sportclub.service.llmmodel.LlmModelService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LlmModelController {

    private final LlmModelService llmModelService;

    @GetMapping("/api/llm-models")
    @Operation(summary = "Aktiivsete LLM mudelite pärimine",
            description = "Tagastatakse Benchmark-vaates valitavate aktiivsete LLM mudelite list (sorteeritud provideri ja nime järgi). Mudelite puudumisel tagastatakse tühi list.")
    public List<LlmModelDto> findActiveLlmModels() {
        return llmModelService.findActiveLlmModels();
    }
}
