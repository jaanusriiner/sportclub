package ee.sportclub.service.register;

import ee.sportclub.controller.register.dto.RegisterRequestDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.area.Area;
import ee.sportclub.persistence.area.AreaRepository;
import ee.sportclub.persistence.sport.Sport;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ee.sportclub.Error.SPORT_MISSING;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final AreaRepository areaRepository;


    @Transactional
    public void registerUser(RegisterRequestDto registerRequestDto) {
        Area area = getValidArea(registerRequestDto.getAreaId());
        List<Sport> sports = getValidSports(registerRequestDto.getSportIds());
    }

    private List<Sport> getValidSports(@NotNull List<Integer> sportIds) {
        if(sportIds.isEmpty()) {
            throw new ForbiddenException(SPORT_MISSING.getMessage(), SPORT_MISSING.name());
        }
        return null;
    }

    public Area getValidArea(@NotNull Integer areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(()-> new PrimaryKeyNotFoundException("AreaId",areaId));
        return area;

    }


}
