package ee.sportclub.service.training;

import ee.sportclub.Status;
import ee.sportclub.controller.training.dto.*;
import ee.sportclub.controller.user.dto.MyTrainingDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.facility.Facility;
import ee.sportclub.persistence.facility.FacilityRepository;
import ee.sportclub.persistence.training.Training;
import ee.sportclub.persistence.training.TrainingMapper;
import ee.sportclub.persistence.training.TrainingRepository;
import ee.sportclub.persistence.training.trainingdate.TrainingDate;
import ee.sportclub.persistence.training.trainingdate.TrainingDateOverviewRepository;
import ee.sportclub.persistence.training.trainingdate.TrainingDateRepository;
import ee.sportclub.persistence.training.traininggroup.TrainingGroup;
import ee.sportclub.persistence.training.traininggroup.TrainingGroupRepository;
import ee.sportclub.persistence.user.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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
    private final TrainingGroupRepository trainingGroupRepository;
    private final FacilityRepository facilityRepository;
    private final TrainingMapper trainingMapper;
    private final TrainingRepository trainingRepository;

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
        if (updateTrainingDateRequestDto.getDescription() != null) {
            trainingDate.getTraining().setDescription(updateTrainingDateRequestDto.getDescription());
        }
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

    @Transactional
    public TrainingRegisterResponseDto unregisterFromTraining(Integer trainingDateId, Integer userId) {
        getValidUser(userId);
        TrainingDate trainingDate = getValidTrainingDateByIdAndLockIt(trainingDateId);
        int deletedCount = userTrainingRepository.deleteUserTrainingBy(userId, trainingDateId);
        if (deletedCount == 0) {
            throw new ForbiddenException(NOT_REGISTERED.getMessage(), NOT_REGISTERED.name());
        }
        trainingDate.setUserCount(trainingDate.getUserCount() - 1);
        TrainingRegisterResponseDto trainingRegisterResponseDto = new TrainingRegisterResponseDto();
        trainingRegisterResponseDto.setMessage("Oled treeningult edukalt maha võetud");
        return trainingRegisterResponseDto;
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

    @Transactional
    public void createNewTraining(CreateTrainingRequestDto createTrainingRequestDto) {
        User user = getValidUser(createTrainingRequestDto.getTrainerId());
        TrainingGroup trainingGroup = getValidTrainingGroupBy(createTrainingRequestDto.getTrainingGroupId());
        Facility facility = getValidFacilityBy(createTrainingRequestDto.getFacilityId());
        validateUserIsTrainingGroupTrainer(user.getId(), trainingGroup.getId());
        Training training = trainingMapper.toTraining(createTrainingRequestDto);
        training.setTrainingGroup(trainingGroup);
        training.setDefaultFacility(facility);
        training.setName(trainingGroup.getName() + " " + "(" + facility.getName() + ")");
        training.setDefaultEndTime(createTrainingRequestDto.getStartTime().plusMinutes(createTrainingRequestDto.getDuration()));
        LocalDate currentDate = createTrainingRequestDto.getStartDate();
        List<DayOfWeek> selectedDaysOfWeek = translateLettersToWeekdays(createTrainingRequestDto.getWeekdays());
        List<TrainingDate> trainingDates = new ArrayList<>();
        while (!createTrainingRequestDto.getEndDate().isBefore(currentDate)) {
            if (selectedDaysOfWeek.contains(currentDate.getDayOfWeek())) {
                TrainingDate trainingDate = new TrainingDate();
                trainingDate.setTraining(training);
                trainingDate.setFacility(facility);
                trainingDate.setStartDate(currentDate);
                trainingDate.setStartTime(createTrainingRequestDto.getStartTime());
                trainingDate.setDuration(createTrainingRequestDto.getDuration());
                trainingDate.setMaxSize(createTrainingRequestDto.getMaxSize());
                trainingDate.setStatus(Status.STATUS_ACTIVE.getCode());
                trainingDate.setUserCount(0);
                trainingDate.setDateAdded(LocalDate.now());
                trainingDates.add(trainingDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        validateTrainingDatesExist(trainingDates);
        trainingRepository.save(training);
        trainingDateRepository.saveAll(trainingDates);
    }

    private void validateTrainingDatesExist(List<TrainingDate> trainingDates) {
        boolean trainingDateIsPresent = trainingDates.isEmpty();
        if (trainingDateIsPresent) {
            throw new ForbiddenException(NO_TRAINING_DATES.getMessage(), NO_TRAINING_DATES.name());
        }
    }

    private DayOfWeek translateLetterToWeekdays(String letter) {
        return switch (letter) {
            case "E" -> DayOfWeek.MONDAY;
            case "T" -> DayOfWeek.TUESDAY;
            case "K" -> DayOfWeek.WEDNESDAY;
            case "N" -> DayOfWeek.THURSDAY;
            case "R" -> DayOfWeek.FRIDAY;
            case "L" -> DayOfWeek.SATURDAY;
            case "P" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Tundmatu täht" + " " + letter);
        };
    }

    private List<DayOfWeek> translateLettersToWeekdays(String letter) {
        List<DayOfWeek> weekdays = new ArrayList<>();
        String[] weekdaysArray = letter.split(",");
        for (String weekday : weekdaysArray) {
            weekdays.add(translateLetterToWeekdays(weekday));
        }
        return weekdays;
    }

    public Facility getValidFacilityBy(Integer facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("facilityId", facilityId));
    }

    public TrainingGroup getValidTrainingGroupBy(Integer trainingGroupId) {
        return trainingGroupRepository.findById(trainingGroupId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingGroupId", trainingGroupId));
    }

    private void validateUserIsTrainingGroupTrainer(Integer userId, Integer trainingGroupId) {
        boolean trainerIsTrainingGroupTrainer = trainingGroupRepository.trainerIsTrainingGroupTrainer(userId, trainingGroupId);
        if (!trainerIsTrainingGroupTrainer) {
            throw new ForbiddenException(NOT_TRAINING_GROUP_TRAINER.getMessage(), NOT_TRAINING_GROUP_TRAINER.name());
        }
    }

}
