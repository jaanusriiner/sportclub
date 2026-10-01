package ee.sportclub.service.joinApplication;

import ee.sportclub.controller.joinapplication.dto.JoinApplicationRequest;
import ee.sportclub.controller.joinapplication.dto.JoinApplicationResponse;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.DataNotFoundException; // Veendu, et see klass on olemas (või kasuta ResourceNotFoundException)
import ee.sportclub.persistence.joinapplication.JoinApplication;
import ee.sportclub.persistence.joinapplication.JoinApplicationRepository;
import ee.sportclub.persistence.joinapplication.JoinApplicationMapper;
import ee.sportclub.persistence.user.UserTrainingGroupRepository;
import ee.sportclub.persistence.training.traininggroup.TrainingGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Set;

import static ee.sportclub.Error.PRIMARY_KEY_NOT_FOUND;
import static ee.sportclub.Error.JOIN_APPLICATION_UNAVAILABLE;

@Service
@RequiredArgsConstructor
public class JoinApplicationService {

    private final JoinApplicationRepository joinApplicationRepository;
    private final UserTrainingGroupRepository userTrainingGroupRepository;
    private final TrainingGroupRepository trainingGroupRepository;
    private final JoinApplicationMapper joinApplicationMapper;

    public JoinApplicationResponse submitApplication(Integer trainingGroupId, JoinApplicationRequest request) {
        Integer userId = request.getUserId();

        if (!trainingGroupRepository.existsById(trainingGroupId)) {
            String message = "Ei leidnud primary keyd 'trainingGroupId' väärtusega: " + trainingGroupId;
            throw new DataNotFoundException(message, PRIMARY_KEY_NOT_FOUND.name());
        }

        Set<Integer> userGroupIds = userTrainingGroupRepository.findTrainingGroupIdsByUserId(userId);
        boolean isAlreadyMember = userGroupIds.contains(trainingGroupId);

        boolean hasPendingApplication = joinApplicationRepository.existsByUserIdAndTrainingGroupIdAndStatus(userId, trainingGroupId, "PEN");

        if (isAlreadyMember || hasPendingApplication) {
            throw new ForbiddenException(JOIN_APPLICATION_UNAVAILABLE.getMessage(), JOIN_APPLICATION_UNAVAILABLE.name());
        }
        JoinApplication joinApplication = new JoinApplication();
        joinApplication.setUserId(userId);
        joinApplication.setTrainingGroupId(trainingGroupId);
        joinApplication.setStatus("PEN");

        joinApplicationRepository.save(joinApplication);

        return joinApplicationMapper.toJoinApplicationResponse("Taotlus esitatud");
    }
}