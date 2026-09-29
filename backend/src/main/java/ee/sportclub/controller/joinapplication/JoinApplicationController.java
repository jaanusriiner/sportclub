package ee.sportclub.controller.joinapplication;

import ee.sportclub.controller.joinapplication.dto.JoinApplicationRequest;
import ee.sportclub.controller.joinapplication.dto.JoinApplicationResponse;
import ee.sportclub.service.joinApplication.JoinApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor

public class JoinApplicationController {
    private final JoinApplicationService joinApplicationService;
    @PostMapping("/api/training-groups/{trainingGroupId}/join-applications")
    public JoinApplicationResponse createJoinApplication(
            @PathVariable Integer trainingGroupId,
            @RequestBody JoinApplicationRequest request
            ) {
        return joinApplicationService.submitApplication(trainingGroupId, request);
    }

}
