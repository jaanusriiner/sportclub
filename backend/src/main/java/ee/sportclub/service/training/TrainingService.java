package ee.sportclub.service.training;

import ee.sportclub.controller.training.dto.*;
import ee.sportclub.controller.user.dto.MyTrainingDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.training.TrainingDate;
import ee.sportclub.persistence.training.TrainingDateOverviewRepository;
import ee.sportclub.persistence.training.TrainingDateRepository;
import ee.sportclub.persistence.user.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static ee.sportclub.Error.*;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingDateOverviewRepository trainingDateOverviewRepository;
    private final TrainingDateRepository trainingDateRepository;
    private final UserRepository userRepository;
    private final UserTrainingGroupRepository userTrainingGroupRepository;
    private final UserTrainingRepository userTrainingRepository;

    public TrainingGroupOverviewPageDto findTrainings(Integer requestUserId, Integer areaId, Integer sportId, Integer trainerId, LocalDate dateFrom, LocalTime timeFrom, Integer page, Integer size) {
        // API lehenumbrid algavad 1-st, Springi PageRequest 0-st; page < 1 käsitletakse kui esimest lehte
        int pageIndex = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(pageIndex, size);
        Page<TrainingGroupOverviewDto> trainingGroupOverviewDtoPage = trainingDateOverviewRepository.findTrainingGroupOverviewDtosBy(requestUserId, areaId, sportId, trainerId, dateFrom, timeFrom, pageable);
        return createTrainingGroupOverviewPageDto(trainingGroupOverviewDtoPage);
    }

    public User getValidUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
    }

    @Transactional
    public void deleteTrainingDateBy(Integer trainingDateId) {
        getValidTrainingDateBy(trainingDateId);
        userTrainingRepository.deleteUserTrainingsBy(trainingDateId);
        trainingDateRepository.deleteById(trainingDateId);
    }

    public TrainingDate getValidTrainingDateBy(Integer trainingDateId) {
        return trainingDateRepository.findById(trainingDateId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingDateId", trainingDateId));
    }

    public List<MyTrainingDto> getUpcomingTrainingsByUserId(Integer userId) {
        getValidUser(userId);
        return trainingDateOverviewRepository.findUpcomingUserTrainingDtosBy(userId);
    }

    @Transactional
    public void updateTrainingDateDetails(Integer trainingDateId, UpdateTrainingDateRequestDto updateTrainingDateRequestDto) {
        TrainingDate trainingDate = getValidTrainingDateBy(trainingDateId);
        validateTrainingDateNewMaxSizeIsAllowed(trainingDate.getUserCount(), updateTrainingDateRequestDto.getMaxSize());
        trainingDate.getTraining().setDescription(updateTrainingDateRequestDto.getDescription());
        trainingDate.setMaxSize(updateTrainingDateRequestDto.getMaxSize());
        trainingDate.setStartDate(updateTrainingDateRequestDto.getTrainingDate());
        trainingDate.setStartTime(updateTrainingDateRequestDto.getTrainingTime());
    }

    @Transactional
    public TrainingRegisterResponseDto registerToTraining(Integer trainingDateId, TrainingDateRegisterRequestDto trainingDateRegisterRequestDto) {
        User user = getValidUser(trainingDateRegisterRequestDto.getUserId());
        TrainingDate trainingDate = getValidTrainingDateByIdAndLockIt(trainingDateId);
        validateUserIsTrainingGroupMember(user, trainingDate);
        validateUserIsRegisteredToTraining(user.getId(), trainingDateId);
        validateTrainingDateHasFreeSpaces(trainingDate.getUserCount(), trainingDate.getMaxSize());
        saveUserToTrainingDate(user, trainingDate);
        trainingDate.setUserCount(trainingDate.getUserCount() + 1);
        return createRegisteredToTrainingSuccessMessage();


    }

    private TrainingDate getValidTrainingDateByIdAndLockIt(Integer trainingDateId) {
        return trainingDateRepository.findTrainingDateByIdAndLockIt(trainingDateId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingDateId", trainingDateId));
    }

    private void validateUserIsTrainingGroupMember(User user, TrainingDate trainingDate) {
        boolean userIsTrainingGroupMember = userTrainingGroupRepository.userIsTrainingGroupMember(trainingDate.getTraining().getTrainingGroup().getId(), user.getId());
        if (!userIsTrainingGroupMember) {
            throw new ForbiddenException(NOT_TRAINING_GROUP_MEMBER.getMessage(), NOT_TRAINING_GROUP_MEMBER.name());
        }
    }

    private void validateUserIsRegisteredToTraining(Integer userId, Integer trainingDateId) {
        boolean userIsRegisteredToTraining = userTrainingRepository.userIsRegisteredToTraining(userId, trainingDateId);
        if (userIsRegisteredToTraining) {
            throw new ForbiddenException(ALREADY_REGISTERED.getMessage(), ALREADY_REGISTERED.name());
        }
    }

    private void validateTrainingDateHasFreeSpaces(Integer userCount, Integer maxSize) {
        if (userCount >= maxSize) {
            throw new ForbiddenException(TRAINING_FULL.getMessage(), TRAINING_FULL.name());
        }

    }

    private void saveUserToTrainingDate(User user, TrainingDate trainingDate) {
        UserTraining userTraining = new UserTraining();
        userTraining.setUser(user);
        userTraining.setTrainingDate(trainingDate);
        userTrainingRepository.save(userTraining);
    }

    private void validateTrainingDateNewMaxSizeIsAllowed(Integer userCount, Integer maxSize) {
        if (userCount > maxSize) {
            throw new ForbiddenException(MAX_SIZE_TOO_LOW.getMessage(), MAX_SIZE_TOO_LOW.name());
        }
    }

    private static TrainingRegisterResponseDto createRegisteredToTrainingSuccessMessage() {
        TrainingRegisterResponseDto trainingRegisterResponseDto = new TrainingRegisterResponseDto();
        trainingRegisterResponseDto.setMessage("Oled edukalt treeningule registreerunud");
        return trainingRegisterResponseDto;
    }

    private static TrainingGroupOverviewPageDto createTrainingGroupOverviewPageDto(Page<TrainingGroupOverviewDto> trainingGroupOverviewDtoPage) {
        TrainingGroupOverviewPageDto trainingGroupOverviewPageDto = new TrainingGroupOverviewPageDto();
        trainingGroupOverviewPageDto.setTrainings(trainingGroupOverviewDtoPage.getContent());
        trainingGroupOverviewPageDto.setTotalElements(trainingGroupOverviewDtoPage.getTotalElements());
        trainingGroupOverviewPageDto.setTotalPages(trainingGroupOverviewDtoPage.getTotalPages());
        return trainingGroupOverviewPageDto;
    }

}
