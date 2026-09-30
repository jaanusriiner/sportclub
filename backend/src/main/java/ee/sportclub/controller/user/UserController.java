package ee.sportclub.controller.user;

import ee.sportclub.controller.user.dto.MyTrainingDto;
import ee.sportclub.infrastructure.error.ApiError;
import ee.sportclub.service.training.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final TrainingService trainingService;

    @GetMapping("/api/users/{userId}/trainings")
    @Operation(
            summary = "Leiab kõik kasutaja tulevased treeningud ja tagastab need massiivina",
            description = """
                    - Vajab sisse userId
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "'message': Ei leidnud primary keyd 'userId', 'errorCode': 'PRIMARY_KEY_NOT_FOUND'",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            ),

    })
    public List<MyTrainingDto> getUpcomingUserTrainings(@PathVariable Integer userId) {
        return trainingService.getUpcomingTrainingsByUserId(userId);
    }

}
