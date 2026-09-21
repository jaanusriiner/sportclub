package ee.sportclub.service.register;

import ee.sportclub.Error;
import ee.sportclub.Status;
import ee.sportclub.controller.register.dto.RegisterRequestDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.area.Area;
import ee.sportclub.persistence.area.AreaRepository;
import ee.sportclub.persistence.sport.Sport;
import ee.sportclub.persistence.sport.SportRepository;
import ee.sportclub.persistence.user.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ee.sportclub.Error.SPORT_MISSING;
import static ee.sportclub.Error.USER_UNAVAILABLE;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final AreaRepository areaRepository;
    private final SportRepository sportRepository;
    private final UserRepository userRepository;

    @Transactional
    public void registerUser(RegisterRequestDto registerRequestDto) {
        validateUserEmailIsAvailable(registerRequestDto.getEmail());
        Area area = getValidArea(registerRequestDto.getAreaId());
        List<Sport> sports = getValidSports(registerRequestDto.getSportIds());
    }

    private void validateUserEmailIsAvailable(@NotNull @Email String email) {
        boolean userEmailIsNotAvailable = userRepository.userEmailIsTakenByActiveUser(email, Status.STATUS_ACTIVE.getCode());
        if (userEmailIsNotAvailable) {
            throw new ForbiddenException(USER_UNAVAILABLE.getMessage(),USER_UNAVAILABLE.name());
        }
    }

    private List<Sport> getValidSports(@NotNull List<Integer> sportIds) {
        if(sportIds.isEmpty()) {
            throw new ForbiddenException(SPORT_MISSING.getMessage(), SPORT_MISSING.name());
        }
        return sportRepository.findAllById(sportIds);
    }

    public Area getValidArea(@NotNull Integer areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(()-> new PrimaryKeyNotFoundException("AreaId",areaId));
        return area;

    }


}
