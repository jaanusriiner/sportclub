package ee.sportclub.controller.joinapplication;

import ee.sportclub.controller.joinapplication.dto.JoinApplicationRequest;
import ee.sportclub.controller.joinapplication.dto.JoinApplicationResponse;
import ee.sportclub.service.joinApplication.JoinApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/api/join-applications/{joinApplicationId}/confirm")
    public JoinApplicationResponse confirmJoinApplication(@PathVariable Integer joinApplicationId) {

        return joinApplicationService.confirmJoinApplication(joinApplicationId);
    }

    @PutMapping("/api/join-applications/{joinApplicationId}/reject")
    public JoinApplicationResponse rejectJoinApplication(@PathVariable Integer joinApplicationId) {

        return joinApplicationService.rejectJoinApplication(joinApplicationId);
    }

}
