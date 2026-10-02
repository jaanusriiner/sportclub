package ee.sportclub.service.facility;

import ee.sportclub.Error;
import ee.sportclub.UserRole;
import ee.sportclub.controller.facility.dto.CreateFacilityRequestDto;
import ee.sportclub.controller.facility.dto.FacilityDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.infrastructure.util.StringBytesConverter;
import ee.sportclub.persistence.area.Area;
import ee.sportclub.persistence.facility.*;
import ee.sportclub.persistence.sport.Sport;
import ee.sportclub.persistence.sport.SportRepository;
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


    public List<FacilityDto> findFacilities() {
        Sort byNameAsc = Sort.by(Sort.Direction.ASC, "name");
        List<Facility> facilities = facilityRepository.findAll(byNameAsc);
        return facilityMapper.toFacilityDtos(facilities);
    }

    @Transactional
    public void addFacilityLocation(CreateFacilityRequestDto createFacilityRequestDto) {
        User user = getValidAdminUserBy(createFacilityRequestDto.getAdminId());
        validateUserHasAdminRole(user);
        Area area = registerService.getValidArea(createFacilityRequestDto.getAreaId());
        List<Sport> sports = getValidSports(createFacilityRequestDto.getSportIds());
        validateFacilityNameIsAvailable(createFacilityRequestDto.getFacilityName());
        Facility facility = facilityMapper.toFacility(createFacilityRequestDto);
        facility.setArea(area);
        facilityRepository.save(facility);
        createSportFacilities(sports, facility);
        handleFacilityImage(createFacilityRequestDto.getImageData(), facility);
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
}