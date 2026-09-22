package ee.sportclub.service.register;

import ee.sportclub.Status;
import ee.sportclub.UserRole;
import ee.sportclub.controller.register.dto.RegisterRequestDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.area.Area;
import ee.sportclub.persistence.area.AreaRepository;
import ee.sportclub.persistence.role.Role;
import ee.sportclub.persistence.role.RoleRepository;
import ee.sportclub.persistence.sport.Sport;
import ee.sportclub.persistence.sport.SportRepository;
import ee.sportclub.persistence.user.User;
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
    private final RoleRepository roleRepository;

    @Transactional
    public void registerUser(RegisterRequestDto registerRequestDto) {
        validateUserEmailIsAvailable(registerRequestDto.getEmail());
        Area area = getValidArea(registerRequestDto.getAreaId());
        List<Sport> sports = getValidSports(registerRequestDto.getSportIds());
        createAndSaveUser(registerRequestDto);


    }

    private void createAndSaveUser(RegisterRequestDto registerRequestDto) {
        User user = createUser(registerRequestDto.getEmail(), registerRequestDto.getPassword(), Status.STATUS_ACTIVE.getCode());
        userRepository.save(user);

    }


    //todo - Jaanus teeb veahandlingu korda
    //todo - Jaanus vaata üle Entity objekti mapping, kas teha mapperis või kuidagi elegantsemalt
    private User createUser(String email, String password, String status) {
        Role role = roleRepository.findByNameIgnoreCase(UserRole.CUSTOMER.getCode())
                .orElseThrow(()-> new PrimaryKeyNotFoundException("roleId",99));
        User user = new User();
        user.setRole(role);
        user.setEmail(email);
        user.setPassword(password);
        user.setStatus(status);

        return user;
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
