package ee.sportclub.service.trainer;

import ee.sportclub.controller.trainer.TrainerSportclubDto;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainer;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainerMapper;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final SportclubTrainerRepository sportclubTrainerRepository;
    private final SportclubTrainerMapper sportclubTrainerMapper;

    public List<TrainerSportclubDto> findTrainerSportclubs(Integer trainerId) {
        List<SportclubTrainer> sportclubTrainers = sportclubTrainerRepository.findSportClubsByTrainer(trainerId);
        List<TrainerSportclubDto> trainerSportclubDtos = sportclubTrainerMapper.toTrainerSportclubDtos(sportclubTrainers);
        return trainerSportclubDtos;
    }


}
