package ee.sportclub.service.traininggroup;

import ee.sportclub.Error;
import ee.sportclub.UserRole;
import ee.sportclub.controller.traininggroup.TrainingGroupManagementDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.joinapplication.JoinApplicationRepository;
import ee.sportclub.persistence.training.TrainingRepository;
import ee.sportclub.persistence.training.trainingdate.TrainingDateRepository;
import ee.sportclub.persistence.training.traininggroup.TrainingGroup;
import ee.sportclub.persistence.training.traininggroup.TrainingGroupRepository;
import ee.sportclub.persistence.user.User;
import ee.sportclub.persistence.user.UserRepository;
import ee.sportclub.persistence.user.UserTrainingGroupRepository;
import ee.sportclub.persistence.user.UserTrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingGroupManagementService {

    private final TrainingGroupRepository trainingGroupRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingDateRepository trainingDateRepository;
    private final UserRepository userRepository;
    private final UserTrainingRepository userTrainingRepository;
    private final UserTrainingGroupRepository userTrainingGroupRepository;
    private final JoinApplicationRepository joinApplicationRepository;

    // admin näeb kõiki treeninggruppe, treener ainult enda omi
    public List<TrainingGroupManagementDto> findTrainingGroups(Integer userId) {
        User user = getValidUserBy(userId);
        Integer trainerId = userIsAdmin(user) ? 0 : userId;
        return trainingGroupRepository.findTrainingGroupManagementDtosBy(trainerId);
    }

    // kustutab grupi koos treeningute, treeningkordade, registreerumiste, liikmesuste ja taotlustega;
    // kasutajaid ega treenereid ei kustutata
    @Transactional
    public void deleteTrainingGroup(Integer trainingGroupId, Integer userId) {
        TrainingGroup trainingGroup = getValidTrainingGroupBy(trainingGroupId);
        User user = getValidUserBy(userId);
        validateUserCanManageTrainingGroup(user, trainingGroup);
        trainingDateRepository.deleteReviewsBy(trainingGroupId);
        userTrainingRepository.deleteUserTrainingsByTrainingGroup(trainingGroupId);
        trainingDateRepository.deleteTrainingDatesBy(trainingGroupId);
        trainingRepository.deleteTrainingsBy(trainingGroupId);
        userTrainingGroupRepository.deleteUserTrainingGroupsBy(trainingGroupId);
        joinApplicationRepository.deleteJoinApplicationsBy(trainingGroupId);
        trainingGroupRepository.deleteById(trainingGroupId);
    }

    private User getValidUserBy(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
    }

    private TrainingGroup getValidTrainingGroupBy(Integer trainingGroupId) {
        return trainingGroupRepository.findById(trainingGroupId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingGroupId", trainingGroupId));
    }

    private void validateUserCanManageTrainingGroup(User user, TrainingGroup trainingGroup) {
        boolean isTrainingGroupTrainer = trainingGroup.getUser().getId().equals(user.getId());
        if (!userIsAdmin(user) && !isTrainingGroupTrainer) {
            throw new ForbiddenException(Error.NOT_TRAINING_GROUP_TRAINER.getMessage(), Error.NOT_TRAINING_GROUP_TRAINER.name());
        }
    }

    private static boolean userIsAdmin(User user) {
        return UserRole.ADMIN.getCode().equals(user.getRole().getName());
    }
}
