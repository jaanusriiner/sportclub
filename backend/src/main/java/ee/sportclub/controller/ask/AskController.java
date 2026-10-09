package ee.sportclub.controller.ask;

import ee.sportclub.controller.ask.dto.AskRequest;
import ee.sportclub.controller.ask.dto.AskResponse;
import ee.sportclub.service.ask.NlToSearchService;
import ee.sportclub.service.ask.NlToSearchService2;
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

    @PostMapping("/api/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return nlToSearchService.ask(request.question(), request.history());
    }

    @PostMapping("/api/ask2")
    public AskResponse ask2(@Valid @RequestBody AskRequest request) {
        return nlToSearchService2.ask(request.question(), request.history());
    }

}
