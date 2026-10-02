package ee.sportclub.service.facility;

import ee.sportclub.Error;
import ee.sportclub.UserRole;
import ee.sportclub.controller.facility.dto.CreateFacilityRequestDto;
import ee.sportclub.controller.facility.dto.FacilityDetailDto;
import ee.sportclub.controller.facility.dto.FacilityDto;
import ee.sportclub.controller.facility.dto.FacilityImageDto;
import ee.sportclub.controller.facility.dto.UpdateFacilityRequestDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.infrastructure.util.StringBytesConverter;
import ee.sportclub.persistence.area.Area;
import ee.sportclub.persistence.facility.*;
import ee.sportclub.persistence.sport.Sport;
import ee.sportclub.persistence.sport.SportRepository;
import ee.sportclub.persistence.training.TrainingRepository;
import ee.sportclub.persistence.training.trainingdate.TrainingDateRepository;
import ee.sportclub.persistence.user.User;
import ee.sportclub.persistence.user.UserRepository;
import ee.sportclub.service.register.RegisterService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityMapper facilityMapper;
    private final UserRepository userRepository;
    private final RegisterService registerService;
    private final SportRepository sportRepository;
    private final SportFacilityRepository sportFacilityRepository;
    private final FacilityImageRepository facilityImageRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingDateRepository trainingDateRepository;


    public List<FacilityDto> findFacilities() {
        Sort byNameAsc = Sort.by(Sort.Direction.ASC, "name");
        List<Facility> facilities = facilityRepository.findAll(byNameAsc);
        return facilityMapper.toFacilityDtos(facilities);
    }

    public FacilityDetailDto findFacility(Integer facilityId) {
        Facility facility = getValidFacilityBy(facilityId);
        FacilityDetailDto facilityDetailDto = facilityMapper.toFacilityDetailDto(facility);
        facilityDetailDto.setSportIds(sportFacilityRepository.findSportIdsBy(facilityId));
        return facilityDetailDto;
    }

    public FacilityImageDto findFacilityImage(Integer facilityId) {
        getValidFacilityBy(facilityId);
        String imageData = facilityImageRepository.findFacilityImageBy(facilityId)
                .map(facilityImage -> StringBytesConverter.bytesToString(facilityImage.getImageBytes()))
                .orElse("");
        return new FacilityImageDto(imageData);
    }

    public Facility getValidFacilityBy(Integer facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("facilityId", facilityId));
    }

    @Transactional
    public void addFacilityLocation(CreateFacilityRequestDto createFacilityRequestDto) {
        validateUserIsAdmin(createFacilityRequestDto.getAdminId());
        Area area = registerService.getValidArea(createFacilityRequestDto.getAreaId());
        List<Sport> sports = getValidSports(createFacilityRequestDto.getSportIds());
        validateFacilityNameIsAvailable(createFacilityRequestDto.getFacilityName());
        Facility facility = facilityMapper.toFacility(createFacilityRequestDto);
        facility.setArea(area);
        facilityRepository.save(facility);
        createSportFacilities(sports, facility);
        handleFacilityImage(createFacilityRequestDto.getImageData(), facility);
    }

    @Transactional
    public void updateFacility(Integer facilityId, UpdateFacilityRequestDto updateFacilityRequestDto) {
        validateUserIsAdmin(updateFacilityRequestDto.getAdminId());
        Facility facility = getValidFacilityBy(facilityId);
        Area area = registerService.getValidArea(updateFacilityRequestDto.getAreaId());
        List<Sport> sports = getValidSports(updateFacilityRequestDto.getSportIds());
        validateFacilityNameIsAvailableForOtherFacility(updateFacilityRequestDto.getFacilityName(), facilityId);
        facilityMapper.updateFacility(updateFacilityRequestDto, facility);
        facility.setArea(area);
        facilityRepository.save(facility);
        sportFacilityRepository.deleteSportFacilitiesBy(facilityId);
        createSportFacilities(sports, facility);
        handleReplaceFacilityImage(updateFacilityRequestDto.getImageData(), facility);
    }

    @Transactional
    public void deleteFacility(Integer facilityId, Integer adminId) {
        validateUserIsAdmin(adminId);
        Facility facility = getValidFacilityBy(facilityId);
        validateFacilityIsNotInUse(facilityId);
        facilityImageRepository.deleteFacilityImagesBy(facilityId);
        sportFacilityRepository.deleteSportFacilitiesBy(facilityId);
        facilityRepository.delete(facility);
    }

    private void validateUserIsAdmin(Integer adminId) {
        User user = getValidAdminUserBy(adminId);
        validateUserHasAdminRole(user);
    }

    private User getValidAdminUserBy(@NotNull Integer adminId) {
        return userRepository.findById(adminId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("adminId", adminId));

    }

    private void validateUserHasAdminRole(User user) {
        if (!user.getRole().getName().equals(UserRole.ADMIN.getCode())) {
            throw new ForbiddenException(Error.NOT_ADMIN.getMessage(), Error.NOT_ADMIN.name());
        }
    }

    private @NonNull List<Sport> getValidSports(List<Integer> sportIds) {
        if (sportIds.isEmpty()) {
            throw new ForbiddenException(Error.SPORT_MISSING.getMessage(), Error.SPORT_MISSING.name());
        }
        List<Sport> sports = sportRepository.findAllById(sportIds);
        if (sports.size() != sportIds.size()) {
            List<Integer> sportIdsInDatabase = new ArrayList<>();
            for (Sport sport : sports) {
                sportIdsInDatabase.add(sport.getId());
            }
            for (Integer sportId : sportIds) {
                if (!sportIdsInDatabase.contains(sportId)) {
                    throw new PrimaryKeyNotFoundException("sportId", sportId);
                }
            }
        }
        return sports;
    }

    private void validateFacilityNameIsAvailable(String facilityName) {
        boolean facilityNameIsUnavailable = facilityRepository.facilityNameIsUnavailable(facilityName);
        if (facilityNameIsUnavailable) {
            throw new ForbiddenException(Error.FACILITY_NAME_UNAVAILABLE.getMessage(), Error.FACILITY_NAME_UNAVAILABLE.name());
        }
    }

    private void validateFacilityNameIsAvailableForOtherFacility(String facilityName, Integer facilityId) {
        boolean facilityNameIsUnavailable = facilityRepository.facilityNameIsUnavailableForOtherFacility(facilityName, facilityId);
        if (facilityNameIsUnavailable) {
            throw new ForbiddenException(Error.FACILITY_NAME_UNAVAILABLE.getMessage(), Error.FACILITY_NAME_UNAVAILABLE.name());
        }
    }

    private void validateFacilityIsNotInUse(Integer facilityId) {
        if (trainingRepository.facilityIsUsedInTrainings(facilityId)
                || trainingDateRepository.facilityIsUsedInTrainingDates(facilityId)) {
            throw new ForbiddenException(Error.FACILITY_IN_USE.getMessage(), Error.FACILITY_IN_USE.name());
        }
    }

    private void createSportFacilities(List<Sport> sports, Facility facility) {
        List<SportFacility> sportFacilities = new ArrayList<>();
        sports.forEach(sport -> {
                    SportFacility sportFacility = new SportFacility();
                    sportFacility.setSport(sport);
                    sportFacility.setFacility(facility);
                    sportFacilities.add(sportFacility);
                }
        );
        sportFacilityRepository.saveAll(sportFacilities);
    }

    private void handleFacilityImage(String imageData, Facility facility) {
        if (imageData != null && !imageData.isEmpty()) {
            FacilityImage facilityImage = new FacilityImage();
            facilityImage.setFacility(facility);
            facilityImage.setImageBytes(StringBytesConverter.stringToBytes(imageData));
            facilityImageRepository.save(facilityImage);
        }
    }

    private void handleReplaceFacilityImage(String imageData, Facility facility) {
        if (imageData != null && !imageData.isEmpty()) {
            facilityImageRepository.deleteFacilityImagesBy(facility.getId());
            handleFacilityImage(imageData, facility);
        }
    }
}
