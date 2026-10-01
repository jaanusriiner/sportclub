package ee.sportclub.service.login;

import ee.sportclub.controller.login.dto.LoginRequestDto;
import ee.sportclub.controller.login.dto.LoginResponseDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.persistence.profile.ProfileRepository;
import ee.sportclub.persistence.user.User;
import ee.sportclub.persistence.user.UserMapper;
import ee.sportclub.persistence.user.UserRepository;
import ee.sportclub.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ee.sportclub.Error.INCORRECT_CREDENTIALS;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProfileRepository profileRepository;

    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
        User user = getValidUser(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        LoginResponseDto loginResponseDto = userMapper.toLoginResponseDto(user);
        handleAddUserFullName(loginResponseDto, user.getId());
        return loginResponseDto;

    }

    private void handleAddUserFullName(LoginResponseDto loginResponseDto, Integer userId) {
        profileRepository.findProfileByUserId(userId)
                .ifPresent(profile -> loginResponseDto.setUserFullName(profile.getFirstName() + " " + profile.getLastName()));
    }

    private User getValidUser(String email, String password) {
        User user = userRepository.findUserBy(email, password, Status.STATUS_ACTIVE.getCode())
                .orElseThrow(() -> new ForbiddenException(INCORRECT_CREDENTIALS.getMessage(), INCORRECT_CREDENTIALS.name()));
        return user;
    }

}
