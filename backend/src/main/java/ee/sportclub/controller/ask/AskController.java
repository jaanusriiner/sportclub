package ee.sportclub.controller.ask;

import ee.sportclub.controller.ask.dto.AskRequest;
import ee.sportclub.controller.ask.dto.AskResponse;
import ee.sportclub.controller.ask.dto.BenchmarkAskRequest;
import ee.sportclub.controller.ask.dto.BenchmarkAskResponse;
import ee.sportclub.service.ask.NlToSearchService;
import ee.sportclub.service.ask.NlToSearchService2;
import ee.sportclub.service.ask.NlToSqlSearchService;
import ee.sportclub.service.ask.NlToSqlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AskController {

    private final NlToSearchService nlToSearchService;
    private final NlToSearchService2 nlToSearchService2;
    private final NlToSqlSearchService nlToSqlSearchService;

    @PostMapping("/api/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return nlToSearchService.ask(request.question(), request.history());
    }

    @PostMapping("/api/ask2")
    public BenchmarkAskResponse ask2(@Valid @RequestBody BenchmarkAskRequest request) {
        return nlToSearchService2.ask(request);
    }

    // Benchmarki SQL-režiim: LLM saab kataloogi ja koostab ise SELECT-i (vt NlToSqlSearchService)
    @PostMapping("/api/ask3")
    public BenchmarkAskResponse ask3(@Valid @RequestBody BenchmarkAskRequest request) {
        return nlToSqlSearchService.ask(request);
    }

}
