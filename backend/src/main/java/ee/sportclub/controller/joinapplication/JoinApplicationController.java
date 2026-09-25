package ee.sportclub.controller.joinapplication;

import ee.sportclub.service.joinApplication.JoinApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor

public class JoinApplicationController {
    private final JoinApplicationService joinApplicationService;


}
