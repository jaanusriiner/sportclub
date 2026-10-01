package ee.sportclub.service.trainer;

import ee.sportclub.UserRole;
import ee.sportclub.controller.joinapplication.dto.PendingJoinApplicationDto;
import ee.sportclub.controller.trainer.TrainerSportclubDto;
import ee.sportclub.controller.trainer.TrainerTrainingGroupDto;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.joinapplication.JoinApplicationRepository;
import ee.sportclub.persistence.sportclub.SportclubRepository;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainer;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainerMapper;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainerRepository;
import ee.sportclub.persistence.training.traininggroup.TrainingGroup;
import ee.sportclub.persistence.training.traininggroup.TrainingGroupMapper;
import ee.sportclub.persistence.training.traininggroup.TrainingGroupRepository;
import ee.sportclub.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final SportclubTrainerRepository sportclubTrainerRepository;
    private final SportclubTrainerMapper sportclubTrainerMapper;
    private final TrainingGroupRepository trainingGroupRepository;
    private final TrainingGroupMapper trainingGroupMapper;
    private final UserRepository userRepository;
    private final JoinApplicationRepository joinApplicationRepository;
    private final SportclubRepository sportclubRepository;

    // admin näeb kõiki spordiklubisid, treener ainult neid, kus ta on tegev
    public List<TrainerSportclubDto> findTrainerSportclubs(Integer trainerId) {
        if (userIsAdmin(trainerId)) {
            return sportclubRepository.findAll(Sort.by("name")).stream()
                    .map(sportclub -> new TrainerSportclubDto(sportclub.getId(), sportclub.getName()))
                    .toList();
        }
        List<SportclubTrainer> sportclubTrainers = sportclubTrainerRepository.findSportClubsByTrainer(trainerId);
        List<TrainerSportclubDto> trainerSportclubDtos = sportclubTrainerMapper.toTrainerSportclubDtos(sportclubTrainers);
        return trainerSportclubDtos;
    }

    public List<TrainerTrainingGroupDto> findTrainerTrainingGroups(Integer trainerId) {
        userRepository.findById(trainerId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainerId", trainerId));

        List<TrainingGroup> trainingGroups = userIsAdmin(trainerId)
                ? trainingGroupRepository.findAll(Sort.by("name"))
                : trainingGroupRepository.findByTrainerId(trainerId);
        return trainingGroupMapper.toTrainerTrainingGroupDtos(trainingGroups);
    }

    public List<PendingJoinApplicationDto> findTrainerPendingJoinApplications(Integer trainerId) {
        userRepository.findById(trainerId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainerId", trainerId));

        if (userIsAdmin(trainerId)) {
            return joinApplicationRepository.findAllPendingApplications();
        }
        return joinApplicationRepository.findPendingApplicationsByTrainerId(trainerId);
    }

    private boolean userIsAdmin(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> UserRole.ADMIN.getCode().equals(user.getRole().getName()))
                .orElse(false);
    }
}
