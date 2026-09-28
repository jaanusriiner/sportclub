package ee.sportclub.service.trainer;

import ee.sportclub.controller.trainer.TrainerSportclubDto;
import ee.sportclub.controller.trainer.TrainerTrainingGroupDto;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainer;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainerMapper;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainerRepository;
import ee.sportclub.persistence.training.TrainingGroup;
import ee.sportclub.persistence.training.TrainingGroupMapper;
import ee.sportclub.persistence.training.TrainingGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final SportclubTrainerRepository sportclubTrainerRepository;
    private final SportclubTrainerMapper sportclubTrainerMapper;
    private final TrainingGroupRepository trainingGroupRepository;
    private final TrainingGroupMapper trainingGroupMapper;

    public List<TrainerSportclubDto> findTrainerSportclubs(Integer trainerId) {
        List<SportclubTrainer> sportclubTrainers = sportclubTrainerRepository.findSportClubsByTrainer(trainerId);
        List<TrainerSportclubDto> trainerSportclubDtos = sportclubTrainerMapper.toTrainerSportclubDtos(sportclubTrainers);
        return trainerSportclubDtos;
    }


    public List<TrainerTrainingGroupDto> findTrainerTrainingGroups(Integer trainerId) {

        List<TrainingGroup> trainingGroups = trainingGroupRepository.findByTrainerId(trainerId);
        return trainingGroupMapper.toTrainerTrainingGroupDtos(trainingGroups);
    }
}
