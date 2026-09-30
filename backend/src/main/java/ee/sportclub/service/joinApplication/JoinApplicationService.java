package ee.sportclub.service.joinApplication;

import ee.sportclub.controller.joinapplication.dto.JoinApplicationRequest;
import ee.sportclub.controller.joinapplication.dto.JoinApplicationResponse;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.DataNotFoundException;
import ee.sportclub.persistence.joinapplication.JoinApplication;
import ee.sportclub.persistence.joinapplication.JoinApplicationRepository;
import ee.sportclub.persistence.joinapplication.JoinApplicationMapper;
import ee.sportclub.persistence.user.User;
import ee.sportclub.persistence.user.UserRepository;
import ee.sportclub.persistence.user.UserTrainingGroup;
import ee.sportclub.persistence.user.UserTrainingGroupRepository;
import ee.sportclub.persistence.training.TrainingGroup;
import ee.sportclub.persistence.training.TrainingGroupRepository;
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
    private final UserRepository userRepository;

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

    public JoinApplicationResponse confirmJoinApplication(Integer joinApplicationId) {
        JoinApplication application = joinApplicationRepository.findById(joinApplicationId)
                .orElseThrow(() -> new DataNotFoundException(
                        "Ei leidnud primary keyd 'joinApplicationId' väärtusega: " + joinApplicationId,
                        PRIMARY_KEY_NOT_FOUND.name()
                ));

        application.setStatus("ACC");
        joinApplicationRepository.save(application);

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Ei leidnud primary keyd 'userId' väärtusega: " + application.getUserId(),
                        PRIMARY_KEY_NOT_FOUND.name()
                ));

        TrainingGroup group = trainingGroupRepository.findById(application.getTrainingGroupId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Ei leidnud primary keyd 'trainingGroupId' väärtusega: " + application.getTrainingGroupId(),
                        PRIMARY_KEY_NOT_FOUND.name()
                ));

        UserTrainingGroup newMember = new UserTrainingGroup();
        newMember.setUser(user);
        newMember.setTrainingGroup(group);

        userTrainingGroupRepository.save(newMember);

        return joinApplicationMapper.toJoinApplicationResponse("Taotlus edukalt kinnitatud");
    }

    public JoinApplicationResponse rejectJoinApplication(Integer joinApplicationId) {
        JoinApplication application = joinApplicationRepository.findById(joinApplicationId)
                .orElseThrow(() -> new DataNotFoundException(
                        "Ei leidnud primary keyd 'joinApplicationId' väärtusega: " + joinApplicationId,
                        PRIMARY_KEY_NOT_FOUND.name()
                ));

        application.setStatus("REJ");
        joinApplicationRepository.save(application);

        return joinApplicationMapper.toJoinApplicationResponse("Taotlus tagasi lükatud");
    }
}