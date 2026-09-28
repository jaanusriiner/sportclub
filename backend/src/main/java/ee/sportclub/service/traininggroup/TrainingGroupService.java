package ee.sportclub.service.traininggroup;

import ee.sportclub.controller.traininggroup.TrainingGroupDto;
import ee.sportclub.persistence.skilllevel.SkillLevelRepository;
import ee.sportclub.persistence.sport.SportRepository;
import ee.sportclub.persistence.sportclub.SportclubRepository;
import ee.sportclub.persistence.training.TrainingGroup;
import ee.sportclub.persistence.training.TrainingGroupMapper;
import ee.sportclub.persistence.training.TrainingGroupRepository;
import ee.sportclub.persistence.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingGroupService {

    private final TrainingGroupRepository trainingGroupRepository;
    private final TrainingGroupMapper trainingGroupMapper;
    private final UserRepository userRepository;
    private final SkillLevelRepository skillLevelRepository;
    private final SportRepository sportRepository;
    private final SportclubRepository sportclubRepository;

    @Transactional
    public void addTrainingGroup(TrainingGroupDto trainingGroupDto) {
        //    validateTrainingGroupInput();
        TrainingGroup trainingGroup = trainingGroupMapper.toTrainingGroup(trainingGroupDto);
        trainingGroup.setUser(userRepository.getReferenceById(trainingGroupDto.getTrainerId()));
        trainingGroup.setSkillLevel(skillLevelRepository.getReferenceById(trainingGroupDto.getSkillLevelId()));
        trainingGroup.setSport(sportRepository.getReferenceById(trainingGroupDto.getSportId()));
        trainingGroup.setSportclub(sportclubRepository.getReferenceById(trainingGroupDto.getSportclubId()));
        trainingGroupRepository.save(trainingGroup);

    }


}
