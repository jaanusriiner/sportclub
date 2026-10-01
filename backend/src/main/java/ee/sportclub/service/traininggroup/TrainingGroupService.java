package ee.sportclub.service.traininggroup;

import ee.sportclub.controller.traininggroup.TrainingGroupDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.skilllevel.SkillLevel;
import ee.sportclub.persistence.skilllevel.SkillLevelRepository;
import ee.sportclub.persistence.sport.Sport;
import ee.sportclub.persistence.sport.SportRepository;
import ee.sportclub.persistence.sportclub.Sportclub;
import ee.sportclub.persistence.sportclub.SportclubRepository;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainerRepository;
import ee.sportclub.persistence.training.traininggroup.TrainingGroup;
import ee.sportclub.persistence.training.traininggroup.TrainingGroupMapper;
import ee.sportclub.persistence.training.traininggroup.TrainingGroupRepository;
import ee.sportclub.persistence.user.User;
import ee.sportclub.persistence.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ee.sportclub.Error.NOT_SPORTCLUB_TRAINER;
import static ee.sportclub.Error.SKILL_LEVEL_SPORT_MISMATCH;

@Service
@RequiredArgsConstructor
public class TrainingGroupService {

    private final TrainingGroupRepository trainingGroupRepository;
    private final TrainingGroupMapper trainingGroupMapper;
    private final UserRepository userRepository;
    private final SkillLevelRepository skillLevelRepository;
    private final SportRepository sportRepository;
    private final SportclubRepository sportclubRepository;
    private final SportclubTrainerRepository sportclubTrainerRepository;

    @Transactional
    public void addTrainingGroup(TrainingGroupDto trainingGroupDto) {
        validateTrainingGroupInput(trainingGroupDto);
        TrainingGroup trainingGroup = trainingGroupMapper.toTrainingGroup(trainingGroupDto);
        User user = userRepository.findById(trainingGroupDto.getTrainerId())
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainerId", trainingGroupDto.getTrainerId()));
        trainingGroup.setUser(user);

        SkillLevel skillLevel = skillLevelRepository.findById(trainingGroupDto.getSkillLevelId())
                .orElseThrow(() -> new PrimaryKeyNotFoundException("skillLevelId", trainingGroupDto.getSkillLevelId()));
        trainingGroup.setSkillLevel(skillLevel);

        Sport sport = sportRepository.findById(trainingGroupDto.getSportId())
                .orElseThrow(() -> new PrimaryKeyNotFoundException("sportId", trainingGroupDto.getSportId()));
        trainingGroup.setSport(sport);

        Sportclub sportclub = sportclubRepository.findById(trainingGroupDto.getSportclubId())
                .orElseThrow(() -> new PrimaryKeyNotFoundException("sportclubId", trainingGroupDto.getSportclubId()));
        trainingGroup.setSportclub(sportclub);

        trainingGroupRepository.save(trainingGroup);

    }

    private void validateTrainingGroupInput(TrainingGroupDto trainingGroupDto) {
        if(!sportclubTrainerRepository.existsSportclubByTrainerId(trainingGroupDto.getTrainerId(), trainingGroupDto.getSportclubId())) {
            throw new ForbiddenException(NOT_SPORTCLUB_TRAINER.getMessage(), NOT_SPORTCLUB_TRAINER.name());
        } else if (!skillLevelRepository.existsByIdAndSportId(trainingGroupDto.getSkillLevelId(),trainingGroupDto.getSportId())) {
            throw new ForbiddenException(SKILL_LEVEL_SPORT_MISMATCH.getMessage(), SKILL_LEVEL_SPORT_MISMATCH.name());
        }
    }


}
