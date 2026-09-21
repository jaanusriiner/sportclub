package ee.sportclub.service.register;

import ee.sportclub.controller.register.dto.RegisterRequestDto;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.area.Area;
import ee.sportclub.persistence.area.AreaRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final AreaRepository areaRepository;


    @Transactional
    public void registerUser(RegisterRequestDto registerRequestDto) {
        Area area = getValidArea(registerRequestDto.getAreaId());
    }

    public Area getValidArea(@NotNull Integer areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(()-> new PrimaryKeyNotFoundException("AreaId",areaId));
        return area;

    }


}
