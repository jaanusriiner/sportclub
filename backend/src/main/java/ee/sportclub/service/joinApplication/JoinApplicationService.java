package ee.sportclub.service.joinApplication;

import ee.sportclub.Status;
import ee.sportclub.controller.joinapplication.dto.JoinApplicationRequest;
import ee.sportclub.controller.joinapplication.dto.JoinApplicationResponse;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.DataNotFoundException;
import ee.sportclub.persistence.joinapplication.JoinApplication;
import ee.sportclub.persistence.joinapplication.JoinApplicationRepository;
import ee.sportclub.persistence.joinapplication.JoinApplicationMapper;
import ee.sportclub.persistence.user.UserTrainingGroup;
import ee.sportclub.persistence.user.UserTrainingGroupRepository;
import ee.sportclub.persistence.training.TrainingGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Set;

import static ee.sportclub.Error.*;

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

    public void confirmJoinApplication(Integer joinApplicationId) {
        JoinApplication application = getValidatedApplication(joinApplicationId);

        userTrainingGroupRepository.save(joinApplicationMapper.toUserTrainingGroup(application));
        application.setStatus(Status.STATUS_ACCEPTED.getCode());
        joinApplicationRepository.save(application);
    }

    public void rejectJoinApplication(Integer joinApplicationId) {
        JoinApplication application = getValidatedApplication(joinApplicationId);

        application.setStatus(Status.STATUS_REJECTED.getCode());
        joinApplicationRepository.save(application);
    }

    private JoinApplication getValidatedApplication(Integer joinApplicationId) {
        JoinApplication application = joinApplicationRepository.findById(joinApplicationId)
                .orElseThrow(() -> new DataNotFoundException(
                        "Ei leidnud primary keyd 'joinApplicationId' väärtusega: " + joinApplicationId,
                        PRIMARY_KEY_NOT_FOUND.name()
                ));

        if (!"PEN".equals(application.getStatus())) {
            throw new ForbiddenException(JOIN_APPLICATION_ALREADY_PROCESSED.getMessage(), JOIN_APPLICATION_ALREADY_PROCESSED.name());
        }
        return application;
    }
}