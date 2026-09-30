package ee.sportclub.controller.ask;

import ee.sportclub.controller.ask.dto.AskRequest;
import ee.sportclub.controller.ask.dto.AskResponse;
import ee.sportclub.service.ask.NlToSqlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ask")
@RequiredArgsConstructor
public class AskController {

    private final NlToSqlService nlToSqlService;

    @PostMapping
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return nlToSqlService.ask(request.question());
    }
}
