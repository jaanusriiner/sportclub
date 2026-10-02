package ee.sportclub.service.user;

import ee.sportclub.Error;
import ee.sportclub.Status;
import ee.sportclub.UserRole;
import ee.sportclub.controller.user.dto.UpdateUserRequestDto;
import ee.sportclub.controller.user.dto.UpdateUserSportclubsRequestDto;
import ee.sportclub.controller.user.dto.UserManagementDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.role.Role;
import ee.sportclub.persistence.role.RoleRepository;
import ee.sportclub.persistence.sportclub.Sportclub;
import ee.sportclub.persistence.sportclub.SportclubRepository;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainer;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainerRepository;
import ee.sportclub.persistence.user.User;
import ee.sportclub.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SportclubRepository sportclubRepository;
    private final SportclubTrainerRepository sportclubTrainerRepository;

    public List<UserManagementDto> findUsers(Integer adminId) {
        validateUserIsAdmin(adminId);
        List<UserManagementDto> userManagementDtos = userRepository.findUserManagementDtos();
        addSportclubIds(userManagementDtos);
        return userManagementDtos;
    }

    @Transactional
    public void updateUser(Integer userId, UpdateUserRequestDto updateUserRequestDto) {
        validateUserIsAdmin(updateUserRequestDto.getAdminId());
        User user = getValidUserBy(userId);
        validateUserIsNotAdminSelf(userId, updateUserRequestDto.getAdminId());
        handleUserReactivation(user, updateUserRequestDto.getStatus());
        handleTrainerRoleRemoval(user, updateUserRequestDto.getRoleName());
        user.setRole(getValidRoleBy(updateUserRequestDto.getRoleName()));
        user.setStatus(updateUserRequestDto.getStatus());
    }

    @Transactional
    public void updateUserSportclubs(Integer userId, UpdateUserSportclubsRequestDto updateUserSportclubsRequestDto) {
        validateUserIsAdmin(updateUserSportclubsRequestDto.getAdminId());
        User user = getValidUserBy(userId);
        validateUserIsTrainer(user);
        List<Sportclub> sportclubs = getValidSportclubs(updateUserSportclubsRequestDto.getSportclubIds());
        sportclubTrainerRepository.deleteSportclubTrainersBy(userId);
        sportclubTrainerRepository.saveAll(createSportclubTrainers(user, sportclubs));
    }

    private void validateUserIsAdmin(Integer adminId) {
        User admin = getValidUserBy(adminId);
        if (!userHasRole(admin, UserRole.ADMIN)) {
            throw new ForbiddenException(Error.NOT_ADMIN.getMessage(), Error.NOT_ADMIN.name());
        }
    }

    private User getValidUserBy(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
    }

    private void addSportclubIds(List<UserManagementDto> userManagementDtos) {
        Map<Integer, List<Integer>> sportclubIdsByUserId = sportclubTrainerRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        sportclubTrainer -> sportclubTrainer.getUser().getId(),
                        Collectors.mapping(sportclubTrainer -> sportclubTrainer.getSportclub().getId(), Collectors.toList())));
        userManagementDtos.forEach(userManagementDto -> userManagementDto.setSportclubIds(
                sportclubIdsByUserId.getOrDefault(userManagementDto.getUserId(), new ArrayList<>())));
    }

    // admin ei tohi iseennast deaktiveerida ega oma admini rolli ära võtta
    private void validateUserIsNotAdminSelf(Integer userId, Integer adminId) {
        if (userId.equals(adminId)) {
            throw new ForbiddenException(Error.CANNOT_MODIFY_SELF.getMessage(), Error.CANNOT_MODIFY_SELF.name());
        }
    }

    // taasaktiveerimisel ei tohi sama e-postiga aktiivset kasutajat juba olla
    private void handleUserReactivation(User user, String newStatus) {
        boolean isReactivation = Status.STATUS_ACTIVE.getCode().equals(newStatus)
                && !Status.STATUS_ACTIVE.getCode().equals(user.getStatus());
        if (isReactivation && userRepository.userEmailIsTakenByActiveUser(user.getEmail(), Status.STATUS_ACTIVE.getCode())) {
            throw new ForbiddenException(Error.USER_UNAVAILABLE.getMessage(), Error.USER_UNAVAILABLE.name());
        }
    }

    // spordiklubidega seosed on mõttekad ainult treeneril
    private void handleTrainerRoleRemoval(User user, String newRoleName) {
        if (userHasRole(user, UserRole.TRAINER) && !UserRole.TRAINER.getCode().equals(newRoleName)) {
            sportclubTrainerRepository.deleteSportclubTrainersBy(user.getId());
        }
    }

    private Role getValidRoleBy(String roleName) {
        // roll on DTO-s juba valideeritud (admin|trainer|customer), seega peab see andmebaasis olemas olema
        return roleRepository.findByNameIgnoreCase(roleName).orElseThrow();
    }

    private void validateUserIsTrainer(User user) {
        if (!userHasRole(user, UserRole.TRAINER)) {
            throw new ForbiddenException(Error.USER_NOT_TRAINER.getMessage(), Error.USER_NOT_TRAINER.name());
        }
    }

    // findAllById jätab olematud id-d vaikselt vahele, seepärast kontrollime iga id eraldi
    private List<Sportclub> getValidSportclubs(List<Integer> sportclubIds) {
        List<Sportclub> sportclubs = sportclubRepository.findAllById(sportclubIds);
        List<Integer> foundSportclubIds = sportclubs.stream().map(Sportclub::getId).toList();
        for (Integer sportclubId : sportclubIds) {
            if (!foundSportclubIds.contains(sportclubId)) {
                throw new PrimaryKeyNotFoundException("sportclubId", sportclubId);
            }
        }
        return sportclubs;
    }

    private List<SportclubTrainer> createSportclubTrainers(User user, List<Sportclub> sportclubs) {
        List<SportclubTrainer> sportclubTrainers = new ArrayList<>();
        for (Sportclub sportclub : sportclubs) {
            SportclubTrainer sportclubTrainer = new SportclubTrainer();
            sportclubTrainer.setUser(user);
            sportclubTrainer.setSportclub(sportclub);
            sportclubTrainers.add(sportclubTrainer);
        }
        return sportclubTrainers;
    }

    private static boolean userHasRole(User user, UserRole userRole) {
        return userRole.getCode().equals(user.getRole().getName());
    }
}
