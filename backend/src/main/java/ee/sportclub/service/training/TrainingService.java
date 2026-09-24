package ee.sportclub.service.training;

import ee.sportclub.controller.training.dto.TrainingGroupOverviewDto;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.profile.Profile;
import ee.sportclub.persistence.profile.ProfileRepository;
import ee.sportclub.persistence.training.TrainingDate;
import ee.sportclub.persistence.training.TrainingDateMapper;
import ee.sportclub.persistence.training.TrainingDateRepository;
import ee.sportclub.persistence.user.User;
import ee.sportclub.persistence.user.UserRepository;
import ee.sportclub.persistence.user.UserTrainingGroupRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final UserRepository userRepository;
    private final TrainingDateRepository trainingDateRepository;
    private final TrainingDateMapper trainingDateMapper;
    private final UserTrainingGroupRepository userTrainingGroupRepository;
    private final ProfileRepository profileRepository;


    public User getValidUser(@NotNull Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
        return user;
    }

    public List<TrainingGroupOverviewDto> findTrainings(Integer userId, Integer areaId, Integer sportId, Integer trainerId, LocalDate date, LocalTime time) {
        User user = getValidUser(userId);
        List<TrainingDate> futureTrainingDates = trainingDateRepository.findTrainingBy(areaId, sportId, trainerId, date, time);
        Map<Integer, TrainingDate> trainingDatesByGroupId = new LinkedHashMap<>();
        for (TrainingDate trainingDate : futureTrainingDates) {
            Integer trainingGroupId = trainingDate.getTraining().getTrainingGroup().getId();
            trainingDatesByGroupId.putIfAbsent(trainingGroupId, trainingDate);

        }
        List<TrainingGroupOverviewDto> trainingGroupOverviewDtos = trainingDateMapper.toTrainingGroupOverviewDtos(new ArrayList<>(trainingDatesByGroupId.values()));
        Set<Integer> trainerIds = new HashSet<>();
        Set<Integer> trainingGroupIdsByUserId = userTrainingGroupRepository.findTrainingGroupIdsByUserId(userId);
        for (TrainingGroupOverviewDto trainingGroupOverviewDto : trainingGroupOverviewDtos) {
            boolean containsTrainingGroupId = trainingGroupIdsByUserId.contains(trainingGroupOverviewDto.getTrainingGroupId());
            trainingGroupOverviewDto.setIsTrainingGroupMember(containsTrainingGroupId);
            trainerIds.add(trainingGroupOverviewDto.getTrainerId());
        }
        Set<Profile> trainerProfiles = profileRepository.findByUserId(trainerIds);
        Map<Integer, Profile> profilesByUserId = new HashMap<>();
        for (Profile profile : trainerProfiles) {
            profilesByUserId.put(profile.getUser().getId(), profile);
        }
        for (TrainingGroupOverviewDto trainingGroupOverviewDto : trainingGroupOverviewDtos) {
            Profile trainerProfile = profilesByUserId.get(trainingGroupOverviewDto.getTrainerId());
            String trainerName = trainerProfile.getFirstName() + " " + trainerProfile.getLastName();
            trainingGroupOverviewDto.setTrainerName(trainerName);
        }
        return trainingGroupOverviewDtos;

    }
}
